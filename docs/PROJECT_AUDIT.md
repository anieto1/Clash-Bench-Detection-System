# CBDS — Full Project Audit

**Date:** 2026-09-09
**Method:** Coordinated multi-agent audit. A coordinator read all five design docs, then briefed five specialists — backend core, CWL/scoring, frontend data & pages, frontend UI & design, and infra/build/ops — each auditing its layer *against the documented spec* rather than in isolation.
**Scope:** 78 Java files, 46 frontend files, 4 Flyway migrations, 4 test files, Docker/compose/Prometheus/Grafana config, git state.
**Stated goals driving prioritization:** (1) get it deployed and working, (2) finish/fix the features, (3) portfolio-ready quality.

---

## 1. Executive summary

**The project is in far better shape than "I haven't touched this in months" usually implies.** The backend compiles clean (74 sources, one benign Lombok warning). The frontend typechecks and builds clean with zero errors. The Redis cache config, the CoC API client's tag encoding, the Prometheus service-name wiring, and the Lombok+MapStruct processor ordering are all *correct* — including several places where the obvious bug was avoided. The CBDS scoring algorithm is **mathematically correct against its own spec**, verified example-by-example.

**But the docs claim six milestones "complete" and that is not true.** The gap between claimed and actual state is the single most important thing to internalize before planning work:

| Docs claim | Reality |
|---|---|
| Milestone 2 "Backend Core ✅" incl. scoring + polling | Scoring math correct; **sync has two data-loss paths**; polling is one fixed rate, not the documented adaptive schedule |
| Milestone 3 "API Layer ✅" | Endpoints all exist, but **every 400/404/405 returns HTTP 500** |
| Milestone 4 "Frontend ✅" | Builds clean, but **no page handles errors**, the player page has **zero CBDS content**, and 3 backend endpoints are never called |
| Milestone 5 "Infrastructure ✅" | **`docker compose up -d` cannot succeed** — the image build fails |
| Tests | **Zero.** The one test file is seven empty method bodies that pass vacuously |

**Three findings need action before anything else:**

1. 🔴 **A live CoC API token sits in `.claude/settings.local.json`, which is not gitignored**, in a repo whose origin is public GitHub. Verified: `git check-ignore` exits 1; the file is untracked (`??`). Nothing has leaked, but you have ~143 untracked files pending and one `git add -A` commits the credential. The JWT payload also encodes the IP allowlist, leaking your home public IP.
2. 🔴 **~95% of the project has never been committed.** The last commit predates the entire scoring engine, the entire frontend, all four migrations, and the whole Docker stack. It exists only on one Windows disk.
3. 🔴 **`master` and `origin/main` share no common ancestor** — `git merge-base` returns nothing. GitHub's default branch shows three README-only commits; none of your real work is visible there.

---

## 2. The single most consequential theme: failures are invisible at every layer

This is the finding I'd act on first after securing the repo, because **it is why debugging a deployment will be miserable** and it compounds across all three of your goals.

Your project's most likely runtime failure is a **403 from an IP-locked CoC token** — the docs say so repeatedly. Trace what a user sees when it happens:

1. **CoC API returns 403.** `CocApiConfig` correctly maps it to `CocApiException`. ✅
2. **`GlobalExceptionHandler` builds a proper RFC 9457 `ProblemDetail`** with `detail`, `title`, `reason`, and `cocStatusCode`. ✅
3. **…but the same class declares `@ExceptionHandler(Exception.class)`**, which Spring's `ExceptionHandlerExceptionResolver` matches *before* the framework's own resolvers run. Because the class doesn't extend `ResponseEntityExceptionHandler`, every `NoResourceFoundException`, `HttpMessageNotReadableException`, `MethodArgumentNotValidException`, and `ResponseStatusException` is flattened to **500 "An unexpected error occurred."**
4. **The frontend's `fetchApi` checks `response.ok` but discards the response body**, replacing it with the hardcoded string `` `API error: ${status}` ``. The whole `ProblemDetail` is thrown away.
5. **No page destructures `isError` or `error`.** All five take only `{ data, isLoading }`.
6. **So the user sees the literal text "Clan not found."**

A stale token — the failure mode the docs warn about most — reports itself as a missing clan. Meanwhile **Sync CWL, the app's primary action, is entirely silent**: success, `204 No Content` (no active CWL), and a 403 are visually identical. The spinner stops and nothing changes.

**Fixing this chain is cheap, touches ~6 files, and makes every subsequent debugging session tractable.** Do it before the deployment work, not after.

---

## 3. Layer-by-layer findings

### 3.1 Backend core — `config/`, `common/`, `cocAPI/`, `clan/`, `player/`

**Works:** All 8 documented endpoints exist, correctly named, nothing missing or extra. Package-by-feature layout, records for DTOs, `@ConfigurationProperties` with `@Validated`, constructor injection, MapStruct impls all generate correctly. `#`-tag encoding via `DefaultUriBuilderFactory` + `EncodingMode.VALUES_ONLY` is the right mechanism. Per-endpoint timeouts (10s connect / 30s read). CoC status→exception mapping is clean. `ddl-auto=validate` with Flyway owning the schema. Every entity uses the correct Hibernate-proxy-aware `equals`/`hashCode`.

**Redis caching matches the documented spec precisely** — all seven cache names, correct TTLs, `GenericJacksonJsonRedisSerializer` with `enableDefaultTyping`, and both documented gotchas genuinely avoided (no `@Cacheable` on list-returning methods, no dead stale-checks inside cached bodies).

**Broken:**

- **P0 — catch-all handler flattens all MVC exceptions to 500.** `GlobalExceptionHandler:45`. See §2.
- **P0 — SPA deep links cannot work.** `SpaForwardingController` is designed to catch non-API 404s and forward to `/index.html`, but the handler above intercepts `NoResourceFoundException` first, so the forward never runs — *and* `src/main/resources/static/` is empty with no frontend build plugin in `pom.xml`, so there's no `index.html` to forward to anyway.
- **P0 — `startTracking` poisons its own transaction.** `ClanService:83-88` catches per-member exceptions, but `syncPlayerFromApi` is `@Transactional(REQUIRED)` and *joins* the caller's transaction. One failed member marks it rollback-only; the catch hides it; the outer commit throws `UnexpectedRollbackException`. It logs success, then fails.
- **P0 — `PlayerEquipment` corrupts its own `HashSet`.** Its constructor never sets `playerTag` (all four sibling entities do). `Player.addEquipment` adds to the set *before* `setPlayer()` mutates the hash. Result: `equals()` short-circuits on null tag so de-duplication never happens, and `contains()`/`removeEquipment()` are silent no-ops.
- **P0 — nullable `Integer` DTO fields are immediately force-unboxed** into primitives (`(short)(int) townHallLevel` and ~12 similar sites). This defeats the entire Jackson-3 convention the docs insist on; any omitted CoC field is a 500.
- **P1 — 5N+1 queries on `GET /api/clans/{tag}/members`.** `PlayerMapper` touches all five lazy collections per player: 251 round-trips for a 50-member clan. A naive `@EntityGraph` would cartesian-product; the fix is `@BatchSize`.
- **P1 — N HTTP calls inside one transaction.** `startTracking` makes 50+ sequential CoC calls (30s timeout each) while holding one of ten Hikari connections.
- **P1 — dead refresh layer.** `refreshClan` and `refreshPlayer` have zero callers and no controller exposes them. Once a row exists, there is **no HTTP way to refresh stale data.**
- **P1 — no tag normalization.** `#29gpgulqg` or a `#`-less tag silently 404s. No `@Pattern`, no uppercasing, no `@Valid` on `TrackRequest`.
- **P1 — zero authentication.** `POST`/`DELETE /api/tracking/clans` are anonymous, and each registration fans out into 50+ calls on your IP-locked token — a trivial way to burn your rate limit.
- **P1 — springdoc 2.8.6 is the Spring Boot 3 line** on a Boot 4 project. It drags a *second* Jackson major version (2.20.2 alongside 3.0.4) into the jar. Assume Swagger UI is broken until verified against a running app. A second, independent hazard: `spring.web.resources.static-locations` *replaces* the default list, dropping `classpath:/META-INF/resources/` where the swagger-ui webjar lives.

**Also:** `@EnableJpaAuditing` is inert (no entity uses `@CreatedDate`); `spring-boot-starter-session-data-redis` is unused on a stateless API; `WebConfig`'s `@Profile("dev")` CORS is dead because **no profile is activated anywhere in the repo** — verified across properties, pom, Dockerfile, and both compose files. Local dev only works because Vite proxies `/api`, masking it. **Any split-origin deployment will fail on CORS with no obvious cause.**

---

### 3.2 CWL & scoring — `cwl/`, `scoring/`, migrations

#### The scoring algorithm is correct. The doc is what's wrong.

`SCORING_ALGORITHM.md` contains a **self-contradiction**. Its table says `position_diff <= -5 → +20` and labels negative as "hitting UP." Its own nine worked examples use the opposite convention — and so does the code. Since map position 1 is the *strongest* base, attacker #10 hitting defender #2 yields `diff = +8` and *is* hitting up. The code is right.

All nine documented examples were verified against `CbdsCalculator` line by line:

| Example | Scenario | Doc | Code |
|---|---|---|---|
| 1 | #10→#2, 3★ | 145 | 145 ✓ |
| 2 | #10→#5, 3★ | 130 | 130 ✓ |
| 3 | #8→#8, 3★ | 100 | 100 ✓ |
| 4 | #5→#13, 3★ | 75 | 75 ✓ |
| 5 | #8→#2, 2★ 97% | 110 | 110 ✓ |
| 6 | #8→#6, 2★ 88% | 60 | 60 ✓ |
| 7 | #6→#14, 2★ 75% | 5 | 5 ✓ |
| 8 | #3→#15, 1★ 45% | −55 | −55 ✓ |
| 9 | absent | −100 | −100 ✓ |

Every other component — base score, all 12 destruction branches, TH modifier, top-base bonus, gimme penalty — matches the spec exactly. **Zero arithmetic deviations.** No rounding issues (destruction is an integer percent compared with `>=` against integer thresholds).

**Action: fix `SCORING_ALGORITHM.md:86-88` and `:218-225`. Do not touch `CbdsCalculator`.**

#### The sync pipeline has two data-loss paths

- **P0 — a single unfetchable player kills the entire sync.** `syncParticipants` swallows a `syncPlayerFromApi` failure, then inserts the `CwlParticipant` anyway — but `cwl_participant.player_tag` has an FK to `player(tag)`. Foreign-key violation on flush rolls back **everything**. The catch creates an illusion of resilience while guaranteeing total failure.
- **P0 — same anti-pattern on score persistence.** `calculateAndPersistScores` is caught, but it's `@Transactional` on another bean and joins the outer transaction → `UnexpectedRollbackException`, all sync rows lost, caller gets a 500.

That's **three independent instances of the same bug shape** (with `ClanService.startTracking`): a `catch` around a `@Transactional` callee that joins the caller's transaction. All three need `Propagation.REQUIRES_NEW` or a boundary outside the parent. **Fix them as one task — it's one concept, not three.**

#### Other sync issues

- **P1 — off-season sync returns 404, not 204.** CoC returns 404 when a clan isn't in CWL, and `CocApiConfig` throws on 404 — so `CwlService`'s `null` and `"groupNotFound"` guards are **unreachable dead code**, and `CwlController`'s 204 branch never fires. This is the *common* case ~23 days a month.
- **P1 — sentinel defaults award phantom points.** A defender-resolution miss defaults `mapPos=0, th=1`. `0 <= 3` triggers the top-base bonus and the position ladder maxes out: **+35 of phantom score**. In practice the DB `CHECK BETWEEN 1 AND 50` rejects 0 and aborts the transaction instead. Broken either way.
- **P1 — duplicate `getPlayer` per participant.** `buildFullSnapshot` and `syncPlayerFromApi` each fetch: **70 API calls for a 35-player roster**, doubling rate-limit exposure.
- **P1 — insert-only, never upsert.** Every write is guarded by an existence check, so re-sync produces no duplicates ✅ — but also never *updates*. A war first seen in `preparation` keeps prep-time values forever; map positions and per-day TH never refresh.
- **P1 — long transaction.** Up to 28 war fetches + 70 player fetches inside one `@Transactional`, at 30s timeout each.
- **Season aggregates are never written.** `updateTotals`/`markCompleted` have no callers, and `LeagueGroupResponse` has no league-name or placement field — so `league_name`, `final_placement`, `total_stars`, `is_completed` are permanently null/0/false and surface that way in the API.
- **Leaderboard has no tie-breaker** — equal scores order nondeterministically by fetch order.
- **`@Cacheable` risk:** `getLeaderboard` returns a record containing `List<LeaderBoardEntry>` each containing `List<AttackScoreDto>` — exactly the nested-generic-list-under-default-typing hazard the project already documented. High probability of deserialization failure on cache hit. **Untested.**
- **Split-brain:** the season leaderboard recomputes live; the overall leaderboard reads V4-persisted snapshots written only at sync time. After any scoring change they disagree until every clan/season is re-synced, and there is no recompute endpoint.

#### The scheduler

One `@Scheduled(fixedRateString = "${polling.cwl.interval:300000}")`. **`polling.cwl.interval` is defined nowhere**, so it's always the hardcoded 5 minutes. The documented adaptive schedule (2–5 min active / 30 min prep / hours idle) **was never implemented**; `tracked_clan.poll_interval` exists in the schema and is never read; `PollingProperties.java` was deleted. No backoff — a stale 403 token logs a warning every 5 minutes forever, and off-season logs a 404 warning every 5 minutes per clan.

#### Schema

Solid overall: correct PKs, FKs with CASCADE, sensible CHECK constraints, and **index coverage that actually matches the query patterns** (V4's partial index exactly matches `findAllWithScores()`'s `ORDER BY`). No entity/DDL mismatch — `ddl-auto=validate` should pass.

Notable: `cwl_attack`'s PK `(war_tag, attacker_tag)` **structurally permits only one attack per player per war** — fine for CWL today, silently drops the second attack if that ever changes (`attacksPerMember` is parsed and never checked). `UNIQUE (war_tag, map_position)` on `cwl_war_member` blocks position corrections and any future opponent-lineup storage. `CwlWar.hashCode()` returns `getClass().hashCode()` — a constant for every instance, degrading its `HashSet` to a linear scan. Two SQL views are never referenced by any Java code. The TH ceiling of 20 will hard-fail sync when Clash ships TH 21.

---

### 3.3 Frontend — data layer & pages

**Works:** Clean typecheck (`tsc --noEmit` exit 0). Clean production build (1894 modules, 377 kB JS / 82 kB CSS, no chunk warning). All 11 client functions hit real, correctly-shaped endpoints with correct `%23` encoding — **no phantom endpoints**. Query keys are well-formed, `as const`, fully parameterized, no collisions. `enabled` guards are correct everywhere. **The documented L1 staleTime strategy is genuinely and accurately implemented** (2/5/10 min as specified). Sync invalidation is correct and arguably over-broad rather than incomplete. Types are `any`-free with correct nullability.

**The best feature in the app:** the season leaderboard's expandable per-attack score breakdown, which surfaces every `CbdsCalculator` modifier (`base/dest/pos/th/top/gimme`) and conditionally hides zeros. Its missed-attack sentinel check was verified byte-for-byte against `AttackScore.ABSENT` in the backend. This is genuinely good work.

**Broken:**

- **P0 — no error handling on any page.** All five destructure only `{ data, isLoading }`. Every failure renders as an empty/not-found state. See §2.
- **P0 — `fetchApi` discards the `ProblemDetail` body** and `ApiError` isn't exported, so no consumer can narrow it.
- **P0 — Sync CWL is completely silent** on success, no-op, and failure.
- **P0 — the player profile page has zero CBDS content.** It's a generic CoC player card (TH, trophies, unit levels). `/api/players/{tag}/cwl` and `/api/players/{tag}/stats` exist, are Redis-cached, and are **never called**. Users click a player from a CBDS leaderboard and land on a page that doesn't mention CBDS.
- **P0 — two of three mobile bottom-nav tabs are dead.** `AppShell` links `/clans` and `/players`; `App.tsx` registers neither, and there's **no `*` catch-all route** — so it's a blank content area with no message.
- **P1 — `ScoreBadge` is used for both per-attack and season-total scores**, but its thresholds are per-attack (top bucket ≥100). A full season is 200–800+, so **every leaderboard row lands in the same emerald bucket** — the color coding on your main leaderboard conveys nothing.
- **P1 — the war page shows map positions, never names.** "Missing Attacks" lists `#3 #7 #11`. Neither `AttackDto` nor `WarMemberDto` carries a player name, so this needs a backend change. For the app's stated purpose this panel is close to useless as-is.
- **P1 — "D1/D2" day labels are wrong for partial rosters.** They come from the array index, but the backend `continue`s past war days a player wasn't rostered for — so a player benched on day 1 has their day-2 attack labeled "D1". Needs a `dayNumber` on `AttackScoreDto`.
- **P1 — no all-time leaderboard page** despite `/api/leaderboard/overall` working.
- **P1 — `useUntrackClan` has zero call sites.** Once tracked, a clan is permanent.
- **P1 — `frontend/dist` is never built into the Spring jar** (no frontend build plugin in `pom.xml`), so production SPA serving is inert regardless of the handler bug.
- **`WarMemberDto.townHallLevel` is missing from the TS type** — the exact field V3 was written to add.
- **`retry: 1` globally** means every 404 and 403 is retried once, doubling latency on the most common failures.
- Dashboard's "Wars Analyzed" / "Total War Wins" sum the clans' *lifetime CoC war records* — nothing to do with CWL or CBDS.
- Unit grids render in nondeterministic order (Java `Set` → JSON array).
- No back links or breadcrumbs; `WarDetailPage` is a dead end that doesn't even display which clan or season it belongs to.

**Lint:** 4 errors — three are the standard shadcn `cva` variant-export pattern (noise); one is real (`react-hooks/set-state-in-effect` in `ThemeToggle`).

---

### 3.4 Frontend — UI & design conformance

**Tailwind v4.1.18, CSS-first config.** The design doc's Tailwind block was *partially* translated into the `@theme` block — colors and shadows landed, `fontFamily.body` and `borderRadius.coc` did not.

**Works — and better than expected:**

- **The font pipeline is real.** Lilita One + Nunito + JetBrains Mono, preconnected and loaded, wired into `@theme`, `font-display` used 16×. This is *not* default Inter.
- **Color tokens** — gold/navy ramps, semantic colors, and all six per-TH-level buckets faithfully transcribed.
- **The body radial-gradient composition** is verbatim from the spec.
- **`THBadge`** is the one fully spec-conformant component, correctly extending purple to TH 18–20.
- **`AppShell`'s responsive strategy is the strongest code in the frontend** — desktop sidebar → mobile Sheet drawer → fixed bottom tab bar, with genuine `env(safe-area-inset-bottom)` handling and `viewport-fit=cover`. `min-w-0` correctly applied in four places to prevent flex overflow. The one real `<table>` is wrapped in `overflow-x-auto`.
- Zero dependency rot — all imports resolve, unified `radix-ui` package used correctly.

**Missing — and it's the identity-carrying half:**

| Spec'd | Status |
|---|---|
| Gold metallic gradient (6-stop) | **absent entirely** |
| Gold button gradient — the signature CoC surface | **absent** |
| `.gold-border` / `-double` / `-3d` / `-gradient` | **none of the four** |
| Background textures (`.bg-grid`/`.bg-noise`/`.bg-stripes`/`.bg-vignette`) | **zero of four** |
| Blue glow, inner glow (pressed state) | **absent** |
| Season selector, war day card, attack detail row, stat card, member row | **none built** — all inlined per-page |
| Button variants (gold/blue/green/ghost/tag) | **none** — every button is stock shadcn |
| Header's 2px gold underline | replaced with `border-b border-border/50` |

The agent's summary: it reads as **"a competent dark dashboard with gold text"** rather than Clash of Clans. The color and type layers landed; the *material* layer didn't.

**Dead code is extensive:** 9 of 12 shadcn components never imported — including `tabs` (two pages hand-roll a tab bar *verbatim*), `tooltip` (which the spec wanted for score breakdowns), `card` (pages hand-roll gradient panels 4× instead), and `collapsible`. Four of five animation keyframes are dead, two with no consumer class ever written. `StarDisplay`'s `animate` prop is never passed.

**Theming is half-wired.** `index.css` is explicitly comment-labeled dark-only — no `.dark` selector, no `prefers-color-scheme` block, no light value for any of the 30 tokens. That's *consistent* (so no unreadable-text risk) and matches the spec's intent. But **`ThemeToggle` is dead three ways**: never rendered, targets a `.dark` class with no rules, and its `prefers-color-scheme` branch is provably unreachable because the persist effect writes localStorage before the hydrate effect reads it. Mounting it would produce a partial restyle of shadcn internals — a regression, not a light theme.

**Accessibility on the custom layer is near zero:** the icon-only mobile hamburger has no `aria-label` (it's the sole mobile nav entry point); `SheetContent` has no `SheetTitle` (Radix requires it — runtime warning + unnamed drawer); no `aria-label` on `StarDisplay`, `THBadge`, `ScoreBadge`, or `RankBadge`, so a leaderboard's entire information content is icon-and-number soup to a screen reader; the hand-rolled tab bars have no `role`/`aria-selected`/arrow-key nav. Empty stars at `#374151` on navy are ~1.3:1 — a 0★ attack is visually indistinguishable from no data.

**`THBadge` doesn't handle null `level`** — and `cwl_war_member.town_hall_level` is V3-new and nullable, so pre-V3 rows render an empty box.

---

### 3.5 Infra, build & ops

**Build health (real output):**

| Command | Result |
|---|---|
| `mvnw -DskipTests compile` | **SUCCESS** — 74 sources, `--release 21`, one benign Lombok warning |
| `mvnw -DskipTests package` | **SUCCESS** — 79 MB fat jar |
| `mvnw test` | **FAILURE** — 7/7 scoring tests pass (vacuously), `contextLoads` errors: Docker Desktop not running |

Java 25 locally compiling with `--release 21` is the supported path; Docker builds on real JDK 21. **No version problem.** But `mvn test` has two latent failures behind the Docker one: there is **no `src/test/resources/`** at all, so `coc.api.token=${COC_API_TOKEN}` fails `@NotBlank` validation with no env var; and `TestcontainersConfiguration` provides **only Postgres, no Redis**, while `spring.cache.type=redis`.

**🔴 `docker compose up -d` cannot succeed.** Verified preconditions:
- `Dockerfile:5` runs `npm ci`, then `Dockerfile:6` does `COPY frontend/ ./`
- There is **no `.dockerignore`** (confirmed absent)
- The host has both `frontend/node_modules` and `frontend/dist`

So line 6 overwrites the container's musl-linux `node_modules` with **Windows-built** binaries. Vite 7/Rollup 4 then fails on the platform-specific optional dep (`Cannot find module @rollup/rollup-linux-x64-musl`). **The README's quick-start fails at image build, before any container starts.** This is the highest-impact blocker for your deployment goal, and the fix is a six-line `.dockerignore`.

**Good infra decisions worth keeping:**
- **Genuinely correct 3-stage Dockerfile** with the frontend built *into* the image and served by Boot from `file:/app/static/`. Single container, no nginx needed — the right design here. Backend layer caching (`pom.xml` + `go-offline` before `COPY src`) is done properly.
- `depends_on: condition: service_healthy` on both postgres and redis, with working healthchecks. ✅
- Redis `--maxmemory 128mb --maxmemory-policy allkeys-lfu` — **matches docs exactly.**
- `COC_API_TOKEN` genuinely reaches the container (via both `env_file` and interpolation — redundant but working).
- **`prometheus.yml` targets `app:8080` and the Grafana datasource targets `http://prometheus:9090` — service names, not `localhost`.** The classic bug is *absent*. Note both files only work when merged with `-f docker-compose.yml -f docker-compose.monitoring.yml`; running the monitoring file alone creates a separate network where `app` never resolves.
- **Monitoring lives exclusively in `docker-compose.monitoring.yml`** — the doc claiming it's in the base compose file is wrong; the README is right.
- **All five claimed custom metrics genuinely exist in source**, exactly as documented.

**🔴 But 4 of 9 Grafana panels query series that will never exist:**

| Panel | Why it's empty |
|---|---|
| CoC API Latency p95 | no `_bucket` series — no `publishPercentileHistogram()`, no `management.metrics.distribution.*` property anywhere |
| CWL Sync Duration p95 | same |
| HTTP Request Latency p95 | Boot doesn't enable HTTP histogram buckets by default |
| Redis Cache Hit Ratio | `RedisCacheManager` built without `.enableStatistics()`, so nothing binds `cache_gets_total` |

Three of these are a **three-line properties fix**; the fourth is one builder call.

**Deployment gaps:** no prod profile *at all* (the only profile-aware artifact in the project is dev CORS), no HTTPS/reverse proxy, **no DB backup** (a named volume is not a backup — one `docker compose down -v` destroys your season history), no `restart:` policy on any of five services, no resource limits or `MaxRAMPercentage` (JVM + Postgres + Redis + Prometheus + Grafana will OOM-kill each other on a 1–2 GB VPS), no app healthcheck despite `/actuator/health` being live, unbounded Docker json-file logging, and `logging.level...config=DEBUG` in the package that builds the bearer-token interceptor.

**Security hygiene:** `/actuator/flyway` (full migration history + checksums) and `/actuator/metrics` are exposed unauthenticated on the public port. Postgres 5432 and Redis 6379 are published to the host with `cbds`/no-password. Grafana defaults to `admin/admin` on a published port. `DB_PASSWORD` defaults to `cbds` — which is how a trivially guessable production password ships.

**Two stray files:** the root `nul` (725 bytes of `ping 127.0.0.1` output — someone ran `ping -n 10 127.0.0.1 > nul` from Git Bash, where `nul` isn't a reserved device, so the redirect created a real file). Delete with `rm ./nul`, not `del`. And `src/main/resources/.env` — 0 bytes, but **it is packaged into the jar** as `BOOT-INF/classes/.env`, a future secret-leak vector.

**Git state:**

| Metric | Value |
|---|---|
| Tracked files | 54 |
| Untracked (`-uall`) | 143 |
| Modified tracked | 43 (+2033 / −199) |
| Commits on `master` | 2 |
| `git merge-base master origin/main` | **no common ancestor** |

Never committed: the entire `scoring/` package, all four migrations, the entire `frontend/`, all of `docs/`, the whole Docker/monitoring stack, and **`mvnw`/`.mvn/`** — so `origin/master` isn't even buildable from a clone, on top of `application.properties` being gitignored.

---

## 4. Cross-cutting patterns

Four themes recur across every layer. Fixing them as *concepts* is more efficient than fixing 30 individual tickets.

1. **`catch` around a transaction-joining callee** — 3 independent instances (`ClanService.startTracking`, `CwlService.syncParticipants`, `CwlService` score persist). Each produces `UnexpectedRollbackException` or an FK-violation rollback while logging apparent success. One fix concept: `REQUIRES_NEW` or an outer boundary.
2. **Errors are constructed carefully, then discarded** — a correct `ProblemDetail` flattened to 500 by a catch-all, then thrown away by the client, then never read by any page. See §2.
3. **Built but never wired.** 3 backend endpoints uncalled, `refreshClan`/`refreshPlayer` unreachable, `useUntrackClan` unused, 9 shadcn components unimported, 4 of 5 animations dead, `ThemeToggle` unrendered, `getScoreTier` uncalled, 2 SQL views unreferenced, the tier system nowhere. **A surprising amount of finished work just isn't connected** — this is the cheapest source of visible progress.
4. **Docs drifted from reality.** Six milestones marked ✅ that aren't; `SCORING_ALGORITHM.md` contradicts itself; `PROJECT_SCAFFOLD.md` names `CwlPoller` (actually `CwlPollingScheduler`), lists V1–V3 (there's a V4), and omits `LeaderboardController`, `TrackingController`, `SpaForwardingController`, `WebConfig`, `JPAConfig`, `ScoredAttack`, `OverallLeaderboardResponse`. One doc claims monitoring is in the base compose file; it isn't.

**And the thing that makes all of this riskier than it needs to be: zero test coverage.** `CbdsCalculatorTest` is seven empty method bodies:

```java
@Test
void positionModifier() {
}
```

Seven vacuous passes. The suite is green and verifies nothing. The scoring correctness in §3.2 was established by *hand-checking nine examples*, not by running anything. Every fix in this document is currently unverifiable except by manual clicking.

---

## 5. Prioritized roadmap

Ordered so each phase unblocks the next. Effort estimates assume familiarity with the codebase.

### Phase 0 — Stop the bleeding (≈1 hour, do today)

1. **Rotate the CoC API token** at developer.clashofclans.com. It's in `.claude/settings.local.json` and your shell history.
2. **`echo '.claude/settings.local.json' >> .gitignore`** — before any `git add`.
3. **Remove `.gitignore:34`** (`/src/main/resources/application.properties`). It holds no literal secrets, only `${...}` placeholders, and ignoring it means a clone can't boot.
4. **Delete** the root `nul` (`rm ./nul`) and `src/main/resources/.env`.
5. **Commit everything in themed commits** — not one 143-file blob. Suggested order: Maven wrapper → gitignore/config → migrations V1–V4 → scoring + tests → CoC API DTO refactor → CWL sync/scheduler → Redis caching → frontend → ops (Docker/Prometheus/Grafana) → docs.
6. **Resolve the branch split.** `origin/main` holds only three README commits. Cleanest path: `git branch -m master main`, cherry-pick anything worth keeping from the old `main`, then `git push -u origin main --force-with-lease`, set `main` as GitHub's default, delete `origin/master`. **Don't** `--allow-unrelated-histories` merge — it produces a two-root graph for no benefit. *This discards three README commits, so confirm before forcing.*

### Phase 1 — Make failures visible (≈3–4 hours) ← *do this before deploying*

7. **Scope the catch-all handler** — extend `ResponseEntityExceptionHandler` or drop `@ExceptionHandler(Exception.class)`.
8. **Parse `ProblemDetail` in `fetchApi`**, export `ApiError` with `detail`/`title`/`reason`/`cocStatusCode`.
9. **Render error states on all five pages** — distinct from empty/not-found.
10. **Give Sync CWL visible outcomes** — success / 204-no-CWL / error.
11. **Handle the CoC 404 "not in CWL" case as 204**, so the common off-season path stops looking like a failure.
12. **Add `retry: (n, err) => !(4xx)`** so 403/404 stop double-firing.

Now a bad token *says* it's a bad token, and every later phase is debuggable.

### Phase 2 — Make it deployable (≈1 day)

13. **Add `.dockerignore`** (`frontend/node_modules`, `frontend/dist`, `target/`, `.git/`, `.idea/`, `.env`, `.claude/`). **This is what makes `docker compose up` work at all.**
14. **Wire the frontend build into Maven** so `dist/` lands in `static/` and SPA forwarding has an `index.html`.
15. **Create `application-prod.properties`** — default-less secrets, `exposure.include=health,prometheus`, separate `management.server.port`, `logging.level.com.pm=INFO`, plus the histogram/statistics settings from item 21.
16. **Harden the Dockerfile** — `USER` (it runs as root), `HEALTHCHECK`, `-XX:MaxRAMPercentage=70`.
17. **Add `docker-compose.prod.yml`** — `restart: unless-stopped` everywhere, `mem_limit`, bounded json-file logging, app healthcheck, and **stop publishing 5432/6379**.
18. **Terminate TLS with Caddy** as a fourth service — automatic Let's Encrypt, `app:8080` upstream, and you stop publishing 8080. Put Grafana behind it too.
19. **Fix CORS for the real deployment** — `WebConfig`'s `@Profile("dev")` never activates anywhere, so a split-origin deploy fails with no obvious cause.
20. **Set up `pg_dump` backups.** The only item here with no workaround if it's missing when you need it.
21. **Fix the 4 dead Grafana panels** — three `management.metrics.distribution.percentiles-histogram.*` properties + `.enableStatistics()` on the `RedisCacheManager`.
22. **Verify springdoc actually starts on Boot 4** — hit `/v3/api-docs` and `/swagger-ui/index.html`. If broken, move to the 3.x line or drop it and delete the five `@Tag` annotations.

Provision the VPS, note its static egress IP, regenerate the token for *that* IP, inject via a `chmod 600` env file — never in the image, never in git. **This is what permanently solves your token problem.**

### Phase 3 — Fix correctness (≈1–2 days)

23. **Fix all three transaction/catch bugs as one task** (`REQUIRES_NEW` or outer boundaries).
24. **Stop inserting `CwlParticipant` after a failed player fetch.**
25. **Stop force-unboxing nullable `Integer` API fields** — both in `PlayerService` and in `CwlService`'s `(short)(int)` sites.
26. **Fix `PlayerEquipment`'s constructor** to take `playerTag` like its four siblings.
27. **Remove the `mapPos=0, th=1` sentinel defaults** — skip the attack or resolve properly.
28. **Move HTTP calls out of transactions** in both `startTracking` and `syncLeagueGroup`.
29. **Collapse the duplicate `getPlayer`** — halves API usage per new participant.
30. **Add leaderboard tie-breakers** (attacks made → stars → destruction).
31. **Make war/war-member writes true upserts.**
32. **Kill the two N+1s** — `@BatchSize` on clan members, `findAllById` in `getTrackedClans`.
33. **Add tag normalization + validation** on all five path variables and `TrackRequest`.
34. **Verify or bypass Redis caching of `LeaderBoardResponse`** — the nested-list hazard.

### Phase 4 — Finish the features (≈2–3 days)

35. **Implement the S/A/B/C/D/F tier system** server-side at 800/650/500/350/200 with F = "The Bench", expose `tier` + `rank` on the leaderboard DTO, add a `TierBadge`, and **delete the impostor `getScoreTier`**. This is the app's namesake concept and it exists nowhere.
36. **Separate the two score scales** so `ScoreBadge` stops putting every season total in one bucket.
37. **Wire the player profile to `/players/{tag}/stats` and `/players/{tag}/cwl}`** — it currently has zero CBDS content.
38. **Build the all-time leaderboard page** on the working `/api/leaderboard/overall`.
39. **Add player names to `AttackDto`/`WarMemberDto`** so the war page and Missing Attacks panel are usable.
40. **Add `dayNumber` to `AttackScoreDto`** to fix the D1/D2 labels.
41. **Fix the two dead mobile routes** and add a `*` 404 route.
42. **Add an untrack control** so `useUntrackClan` is reachable.
43. **Implement adaptive polling** (2–5 min active / 30 min prep / hours idle), honor `tracked_clan.poll_interval`, and **add backoff** so a stale token stops warning every 5 minutes forever.
44. **Add authentication** — or at minimum network-restrict the tracking endpoints and actuator. Anonymous `POST /api/tracking/clans` fans out into 50+ calls on your rate-limited token.

### Phase 5 — Portfolio quality (≈2–3 days)

45. **Implement the seven empty test stubs** — start with all nine documented worked examples and every `positionModifier` boundary (−5,−4,−3,−2,2,3,4,5). This is the highest-credibility-per-hour work in the project, and it locks in the algorithm that is currently correct only by luck of never having been edited.
46. **Fix `TestcontainersConfiguration`** — add Redis, pin `postgres:16-alpine`, add `src/test/resources/application.properties` with a dummy token so `contextLoads` becomes meaningful.
47. **Add sync-pipeline and leaderboard tests** — these are the two places with data-loss bugs and zero coverage.
48. **Add a GitHub Actions workflow** running `./mvnw -B verify` + `docker build` on PRs. This makes the Dockerfile bug unable to regress silently and fixes "I can't run the context test locally."
49. **Correct the docs** — fix the inverted position table in `SCORING_ALGORITHM.md`, un-check the milestones that aren't done, fix `PROJECT_SCAFFOLD.md`'s stale filenames and missing classes, fix the monitoring-location contradiction.
50. **Delete the dead code** catalogued in §3 — it's a large, safe, visible cleanup.

### Phase 6 — Visual polish (≈1–2 days, highest leverage first)

51. **Build the gold `.btn-primary` bevel as a `button.tsx` variant.** Every button in the app is currently stock shadcn — this is the most CoC identity per line changed anywhere in the codebase.
52. **Add the gold metallic gradient + gold-border utilities.** This is what makes a card read as Clash of Clans rather than Bootstrap.
53. **Restore the header's 2px gold underline.** One line, large payoff.
54. **Extract `.coc-card` / `.stat-card` / `.member-row`** and replace the four duplicated inline gradient panels.
55. **Swap both hand-rolled tab bars for `ui/tabs.tsx`** — kills a verbatim duplication and gains full keyboard/ARIA from an already-installed component.
56. **Fix accessibility**: `aria-label` on the hamburger, `SheetTitle` in the drawer, labels on all four data badges, focus rings on nav links.
57. **Guard `THBadge` against null**, raise the empty-star contrast above 1.3:1, and move the −100 "did not attack" rule into `ScoreBadge`.
58. **Decide on theming** — either delete `ThemeToggle` (dead, with an unreachable branch) or mount it *and* author a real light token set. Dark-only is a legitimate choice; a dead toggle is not.
59. Add one background texture, wire up the four dead animations, reconcile the radius scale to the spec's chunkier values.

---

## 6. Open questions for you

1. **Discarding `origin/main`'s three README commits** — Phase 0 item 6 needs your confirmation before any force-push.
2. **Auth scope** — is this staying private (friends via a shared URL, so network restriction suffices) or going public (needs real auth + rate limiting)? The docs say "eventually public," which changes item 44 substantially.
3. **Tier names** — `SCORING_ALGORITHM.md` still has `???` for S through D, with only F named "The Bench." Item 35 needs those names.
4. **Season-score normalization** — right now a player in 2 lineups scoring 145 each (290) outranks one in 7 lineups averaging 40 (280). Raw sums with unequal denominators. Is that intended, or should participation rate factor in? This is a real design question about what "should be benched" means.
5. **Regular wars** — still listed as post-MVP. Worth deciding now, since 2-attacks-per-player breaks `cwl_attack`'s `(war_tag, attacker_tag)` primary key.

---

## 7. Bottom line

You have a **working backend, a clean-building frontend, a correct scoring algorithm, and genuinely good infrastructure instincts** — the service-name Prometheus wiring, the single-container frontend-in-Boot design, the `depends_on: service_healthy` gating, and the annotation-processor ordering are all things people commonly get wrong and you didn't.

What's missing is the **last 15% that makes software real**: error visibility, transaction discipline, test coverage, and connecting the parts you already built. Roughly a third of the task list is "wire up something that already works."

The fastest path to a deployed, demonstrable project: **Phase 0 today** (an hour, and it removes a real credential risk), **Phase 1 next** (errors become visible, so everything after is debuggable), then **Phase 2** (`.dockerignore` alone unblocks the entire deployment). That's realistically a weekend to go from "uncommitted on one disk" to "running on a VPS with a token that never breaks again."
