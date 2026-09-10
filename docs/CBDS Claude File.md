# Clash of Clans CWL Tracker - Project Reference

## Project Overview

A lightweight web application to track Clan War League (CWL) performance for Clash of Clans. The primary goal is to identify top performers and underperformers within a clan using the **Clash Bench Detection System (CBDS)** scoring algorithm.

### Core Question
**"Who's carrying their weight, and who should be on the bench?"**

### Target Users
- Initially: Developer and friends (private use)
- Eventually: Public release for any clan to use

### Project Philosophy
- Start lightweight, add complexity only when needed
- Build history going forward (historical data not available via API)
- Focus on CWL first, regular wars later

---

## Tech Stack

| Layer | Technology | Version | Reasoning |
|-------|------------|---------|-----------|
| **Backend** | Java | 25 | Latest features, virtual threads |
| **Framework** | Spring Boot | 4.0.2 | Modern, well-documented, portfolio-worthy |
| **Database** | PostgreSQL | 16 | Great JSON support, free, scales well |
| **Cache** | Redis | 7 | Shared L2 cache, LFU eviction for popular data |
| **Frontend** | React + Vite | Latest | SPA with TanStack Query for data fetching/caching |
| **Monitoring** | Prometheus + Grafana | Latest | Metrics collection + dashboards |
| **API Docs** | springdoc-openapi | 2.8.6 | Swagger UI at /swagger-ui.html |
| **Hosting** | Self-hosted initially | - | Cloudflare Tunnel or ngrok for friends access |
| **Future Hosting** | Cheap VPS ($5-6/mo) | - | When ready for public release |

---

## Clash of Clans Domain Knowledge

### Clan War League (CWL)
- Occurs **monthly** (first week of each month)
- Lasts **7 days** with one war per day
- **15 players** participate each day from a roster of up to 35
- Each player gets **1 attack per day**
- **8 clans** compete in a league group
- Clan with most total stars wins; destruction % is tiebreaker

### Map Position & War Weight
- Players are ranked 1-15 based on **war weight**
- War weight considers: defenses, walls, traps, heroes, laboratory levels
- **Position 1** = strongest base (highest war weight)
- **Position 15** = weakest base (lowest war weight)
- A player's map position can change between days

### Town Hall Levels
- Current max: TH17
- Higher TH generally means stronger base
- All 5 heroes available at high TH levels:
  - Barbarian King
  - Archer Queen
  - Grand Warden
  - Royal Champion
  - Minion Prince

### Pets
- Up to 11 pets available
- Unlocked based on TH level and Pet House level
- All 11 available at TH17 with max Pet House

---

## Clash of Clans API

### Authentication
- API key required (developer has one)
- Keys are IP-restricted
- Base URL: `https://api.clashofclans.com/v1/`

### Available Data

| Endpoint | Data Available | Notes |
|----------|----------------|-------|
| `/players/{tag}` | Full player profile | Heroes, troops, spells, pets, equipment, donations, war stars |
| `/clans/{tag}` | Clan info + member list | Rank, points, W/T/L, description, members with roles |
| `/clans/{tag}/currentwar` | Current war details | Only during active war |
| `/clans/{tag}/currentwar/leaguegroup` | CWL group info | Participating clans, rounds, war tags |
| `/clanwarleagues/wars/{warTag}` | Specific CWL war | Attack details during/after war |
| `/clans/{tag}/warlog` | War history | Last 60 wars, summary only (no attack details) |

### Critical Limitations

1. **No army composition data** — API does not reveal troops/spells used in attacks
2. **No historical CWL details** — Past CWL attack data is not retrievable
3. **War log is summary only** — Just W/L/stars totals, no per-player breakdown
4. **Must capture in real-time** — Detailed war data must be polled during active wars
5. **Public war log required** — Clan must have war log set to public
6. **Rate limits** — ~30-40 requests/second, 429 error if exceeded

### Data We Must Capture Live
- Individual attacks (attacker, defender, stars, destruction)
- Player map positions for each war day
- Player stats at time of participation (for snapshots)

### Data We Can Fetch Anytime
- Current player stats (heroes, troops, etc.)
- Clan information
- War log summaries

---

## Architecture Decisions

### Data Strategy: Hybrid Normalized + JSON Snapshots

**Current State (Normalized)**
- Player's current heroes, troops, spells, pets, equipment
- Updated every time we poll
- Easy to query: "Who has underleveled heroes?"

**Historical State (JSON Snapshots)**
- Frozen at time of CWL participation
- Stored as JSONB on `cwl_participant` record
- Immutable — never changes after creation
- Provides context: "What did they have when they attacked?"

**Rationale:**
- Current data changes constantly → normalized for easy updates/queries
- Historical data never changes → JSON is simpler, fewer tables
- PostgreSQL JSONB allows querying inside snapshots if ever needed

### Polling Strategy

**Tracked Clans Approach:**
- Users register clans they want to track
- System polls tracked clans on interval (default: 5 minutes)
- Must poll frequently during CWL to capture attack data before it's gone

**Polling Priorities:**
| Situation | Poll Interval |
|-----------|---------------|
| CWL active | Every 2-5 minutes |
| CWL prep day | Every 30 minutes |
| No active war | Every few hours (just sync player data) |

### Why Not Real-Time Webhooks?
- CoC API doesn't support webhooks
- Must poll to detect changes
- Industry standard for CoC trackers

### Caching Strategy (Multi-Tier: L1 Browser → L2 Redis → L3 PostgreSQL)

**L1 — Browser (TanStack Query)**
- Per-user in-memory cache, configured via `staleTime` per query
- Dashboard/lists: 2 min, Profiles: 5 min, CWL/wars/leaderboards: 10 min
- Mutations invalidate related queries immediately

**L2 — Redis (Shared Across All Users)**
- The key layer for the "famous clan" use case — popular clans stay hot in cache
- `allkeys-lfu` eviction policy, 128MB maxmemory
- TTLs: profiles 30 min, CWL data 24 hr
- Targeted eviction on sync — only affected keys are evicted, not all entries

**L3 — PostgreSQL (Source of Truth)**
- Always consistent, only hit on L2 cache miss

**Cache Names & TTLs:**

| Cache | TTL | Cached By |
|-------|-----|-----------|
| `clan` | 30 min | `ClanService.getClan()` |
| `player` | 30 min | `PlayerService.getPlayer()` |
| `cwlSeason` | 24 hr | `CwlService.getSeason()` |
| `cwlWar` | 24 hr | `CwlService.getWar()` |
| `leaderboard` | 24 hr | `CwlService.getLeaderboard()` |
| `playerCwlHistory` | 30 min | `PlayerService.getPlayerCwlHistory()` |
| `playerStats` | 30 min | `PlayerService.getPlayerStats()` |

**Why These Choices:**
- **LFU eviction** keeps frequently accessed data warm — a trending clan stays cached
- **Targeted eviction** ensures syncing clan A doesn't destroy clan B's cache
- **L1 browser cache** prevents redundant network requests during navigation
- **No `@Cacheable` on list-returning methods** — Jackson default typing breaks on `List<Record>`

---

## Key Entities & Relationships

```
tracked_clan (which clans we actively poll)
     │
     ▼
   clan ◄──────────────── cwl_season (monthly)
     │                        │
     ▼                        ├──► cwl_participant (roster + JSON snapshot)
  player                      │
     │                        └──► cwl_war (7 per season)
     │                                  │
     ├──► player_hero                   ├──► cwl_war_member (15 per war day)
     ├──► player_equipment              │
     ├──► player_troop                  └──► cwl_attack (individual attacks)
     ├──► player_spell
     └──► player_pet
```

### Key Relationships
- **Clan → Players**: One-to-many (players belong to a clan)
- **CWL Season → Wars**: One-to-many (7 wars per season)
- **CWL Season → Participants**: One-to-many (roster for the season)
- **CWL War → War Members**: One-to-many (15 players per day)
- **CWL War → Attacks**: One-to-many (up to 15 attacks per war)
- **Player → Attacks**: One-to-many (one attack per war day)

---

## Clash Bench Detection System (CBDS)

See: `docs/SCORING_ALGORITHM.md`

### Quick Summary
- Scores calculated **per attack**, summed for season total
- Range: -100 (missed attack) to +145 (exceptional)
- Factors: stars, destruction %, position differential, TH differential
- **Top base bonus** (+15) for attacking positions 1-3
- **Gimme penalty** (-20) for failing to 3-star positions 13-15
- **Missed attack** = -100 (worst possible offense)

### Score Interpretation
| Per-Attack Score | Meaning |
|------------------|---------|
| 130-145 | Exceptional |
| 110-129 | Excellent |
| 100-109 | Great |
| 85-99 | Good |
| 50-84 | Average |
| 20-49 | Below Average |
| 0-19 | Poor |
| Below 0 | Bad |
| -100 | Did not attack |

---

## Features (MVP Scope)

### Must Have (CWL Focus)
- [ ] Track a clan's CWL performance
- [ ] Record all attacks with stars/destruction
- [ ] Calculate CBDS scores per player
- [ ] Display season leaderboard (champs vs bench)
- [ ] Player profile with CWL history
- [ ] Clan profile with CWL history

### Nice to Have (Post-MVP)
- [ ] Regular war tracking
- [ ] Historical trend analysis
- [ ] Multiple clan support
- [ ] Public search (any clan/player lookup)
- [ ] Tier/rank badges
- [ ] Export to spreadsheet

### Out of Scope (For Now)
- User accounts/authentication
- Clan management features
- Push notifications
- Mobile native app

---

## API Endpoints (Planned)

### Clan Endpoints
```
GET  /api/clans/{tag}              — Clan profile
GET  /api/clans/{tag}/members      — Clan member list
GET  /api/clans/{tag}/cwl          — CWL history for clan
GET  /api/clans/{tag}/cwl/{season} — Specific CWL season details
```

### Player Endpoints
```
GET  /api/players/{tag}            — Player profile
GET  /api/players/{tag}/cwl        — Player's CWL history
GET  /api/players/{tag}/stats      — Aggregated CBDS stats
```

### Tracking Endpoints
```
POST /api/tracking/clans           — Start tracking a clan
DELETE /api/tracking/clans/{tag}   — Stop tracking a clan
GET  /api/tracking/clans           — List tracked clans
```

### Leaderboard Endpoints
```
GET  /api/leaderboard/cwl/{season} — Season leaderboard
GET  /api/leaderboard/overall      — All-time leaderboard
```

---

## Hosting Strategy

### Phase 1: Development & Friends
- Run locally on developer's machine
- Expose via **Cloudflare Tunnel** or **ngrok**
- Zero cost
- Friends access via shared URL

### Phase 2: Public Release
- Cheap VPS (~$5-6/month)
- Options: DigitalOcean, Hetzner, Railway, Render, Fly.io
- PostgreSQL can run on same VPS or use managed (e.g., Supabase free tier)
- Frontend on Vercel (free) or same VPS

### Deployment Considerations
- Docker container for easy deployment
- Environment variables for API key, DB credentials
- Health checks for monitoring
- Scheduled job for polling (Spring @Scheduled or external cron)

---

## Project Structure (Actual)

```
Clash Bench Detection System/
├── docs/
│   ├── SCORING_ALGORITHM.md          # CBDS scoring formula
│   ├── API_DATA_MAPPING.md           # CoC API endpoints & DTOs
│   ├── PROJECT_SCAFFOLD.md           # File structure & dependencies
│   └── CBDS Claude File.md           # This file
├── docker-compose.yml                # PostgreSQL, Redis, Prometheus, Grafana
├── prometheus.yml                    # Prometheus scrape config
├── grafana/provisioning/             # Auto-provisioned datasource + dashboard
├── src/main/java/com/pm/clashbenchdetectionsystem/
│   ├── ClashBenchDetectionSystemApplication.java
│   ├── config/                       # CocApiConfig, CocApiProperties, RedisCacheConfig
│   ├── common/exception/             # GlobalExceptionHandler, ResourceNotFoundException, CocApiException
│   ├── cocAPI/                       # CocApiClient (@GetExchange), API response DTOs
│   ├── clan/                         # Clan + TrackedClan entities, ClanService, ClanController
│   ├── player/                       # Player + sub-entities, PlayerService, PlayerController
│   ├── cwl/                          # CWL entities, CwlService, CwlController, CwlMapper
│   └── scoring/                      # CbdsCalculator, CbdsScoreService, SeasonScore
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/                 # V1 (initial), V2 (constraints), V3 (TH per war day)
└── frontend/                         # React + Vite + TanStack Query
    └── src/
        ├── api/client.ts             # API client functions
        ├── hooks/queries.ts          # TanStack Query hooks with L1 caching
        ├── pages/                    # DashboardPage, ClanPage, PlayerPage, etc.
        └── components/               # Reusable UI components
```

---

## Open Questions & Future Decisions

1. **Tier names for CBDS rankings** — Still TBD, F-tier is "The Bench"

2. **How to handle players who leave mid-CWL?** — Their attacks still count, snapshot preserved

3. **What if a clan's war log is private?** — Can't track them, show error message

4. **Multi-season aggregation** — How to weight recent vs old seasons?

5. **Regular war support** — Different scoring needed? (2 attacks vs 1)

---

## Reference Files

| File | Purpose |
|------|---------|
| `docs/SCORING_ALGORITHM.md` | Full CBDS scoring documentation with examples |
| `docs/API_DATA_MAPPING.md` | CoC API endpoints, Java DTOs, polling workflows |
| `docs/PROJECT_SCAFFOLD.md` | File structure & dependency listing |
| `docs/CBDS Claude File.md` | This project reference file |
| `docker-compose.yml` | All services: PostgreSQL, Redis, app, Prometheus, Grafana |
| `prometheus.yml` | Prometheus scrape configuration |
| `frontend/src/hooks/queries.ts` | TanStack Query hooks with L1 cache config |

---

## Key Technical Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| **Equipment source** | Nested `heroes[].equipment[]` | Shows what's actively equipped (2 per hero), not just owned |
| **Pet storage** | Separate from troops | API mixes them in `troops[]`, we filter by known pet names |
| **Village filter** | Home only | Exclude Builder Base (Battle Machine, etc.) |
| **Snapshot timing** | First attack, then frozen | Mid-CWL upgrades are rare at high TH; snapshot is for post-CWL discussions |
| **Snapshot purpose** | Clan retrospectives | Discuss what went wrong, what to prioritize upgrading |
| **War tag discovery** | Fetch each round's 4 wars, match our clan tag | League group has root `tag` field identifying our clan |

---

## Development Milestones

### Milestone 1: Foundation ✅
- [x] Define requirements
- [x] Design database schema
- [x] Design scoring algorithm (CBDS)
- [x] Document project decisions
- [x] Map CoC API endpoints and create Java DTOs

### Milestone 2: Backend Core ✅
- [x] Initialize Spring Boot 4.0.2 project
- [x] Implement CoC API client (RestClient + @GetExchange)
- [x] Implement database entities & repositories (Flyway V1-V3)
- [x] Implement CBDS scoring service (CbdsCalculator + CbdsScoreService)
- [x] Implement CWL polling scheduler

### Milestone 3: API Layer ✅
- [x] REST controllers for clans/players
- [x] Leaderboard endpoints
- [x] Tracking management endpoints
- [x] springdoc-openapi Swagger UI

### Milestone 4: Frontend ✅
- [x] React + Vite + TanStack Query SPA
- [x] CoC-themed dashboard with hero section
- [x] Clan profile page
- [x] Player profile page
- [x] Leaderboard page
- [x] Search functionality

### Milestone 5: Infrastructure ✅
- [x] Docker Compose (PostgreSQL, Redis, app, Prometheus, Grafana)
- [x] Multi-tier caching (L1 browser, L2 Redis, L3 PostgreSQL)
- [x] Prometheus metrics (API latency, sync duration, counters)
- [x] Grafana dashboards (auto-provisioned datasource + dashboard)
- [x] GlobalExceptionHandler with proper HTTP status mapping

### Milestone 6: Deployment (In Progress)
- [ ] Deploy to cloud VPS with static IP (fixes CoC API token IP-lock)
- [ ] Test with real CWL data during active CWL season
- [ ] Iterate based on feedback

---

## Notes for Future Development

### When Adding Regular Wars
- Different structure: 2 attacks per player
- War log gives last 60 wars (summary only)
- Need different scoring weights (or modifier)
- May need separate tables or flags on existing tables

### When Adding Multiple Clan Support
- Frontend: clan selector/switcher
- API: scope queries by clan
- Polling: manage multiple clan schedules

### Performance Considerations
- Index on `player_tag` across attack tables
- Consider materialized views for leaderboards
- Cache frequently accessed data (clan profiles)
- Batch inserts when syncing large data sets
