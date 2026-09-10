# CBDS - Project Scaffold

This document outlines the file structure and dependencies for the Clash Bench Detection System.

---

## Project Structure

### Root Files
```
Clash Bench Detection System/
├── pom.xml                              # Maven build (Spring Boot 4.0.2)
├── docker-compose.yml                   # PostgreSQL, Redis, app, Prometheus, Grafana
├── prometheus.yml                       # Prometheus scrape config
├── .env                                 # COC_API_TOKEN (Docker Compose only)
├── docs/
│   ├── CBDS Claude File.md              # Project reference
│   ├── SCORING_ALGORITHM.md             # CBDS documentation
│   ├── API_DATA_MAPPING.md              # API endpoints & DTOs
│   └── PROJECT_SCAFFOLD.md              # This file
└── grafana/provisioning/
    ├── datasources/datasource.yml       # Auto-provisioned Prometheus datasource
    └── dashboards/
        ├── dashboard.yml                # Dashboard provider config
        └── cbds-dashboard.json          # CBDS Grafana dashboard
```

### Application Entry
```
src/main/java/com/pm/clashbenchdetectionsystem/
└── ClashBenchDetectionSystemApplication.java
```

### Config Package
```
src/main/java/com/pm/clashbenchdetectionsystem/config/
├── CocApiProperties.java               # @ConfigurationProperties for CoC API token
├── CocApiConfig.java                   # RestClient bean + Bearer interceptor + error handler
└── RedisCacheConfig.java               # @EnableCaching, cache names, TTLs, serializer
```

### Common Package
```
src/main/java/com/pm/clashbenchdetectionsystem/common/
└── exception/
    ├── GlobalExceptionHandler.java      # @RestControllerAdvice, status code mapping
    ├── ResourceNotFoundException.java   # Static factories: clan(), player(), cwlSeason(), cwlWar()
    └── CocApiException.java            # Wraps CoC API error responses
```

### CoC API Client Package
```
src/main/java/com/pm/clashbenchdetectionsystem/cocAPI/
├── CocApiClient.java                   # @GetExchange declarative HTTP interface
└── dto/
    ├── CocClanResponse.java            # Clan + member list
    ├── CocPlayerResponse.java          # Player + heroes/troops/spells/pets/equipment
    ├── CocCwlWarResponse.java          # CWL war details (members, attacks)
    └── LeagueGroupResponse.java        # League group (8 clans, 7 rounds, war tags)
```

### Clan Domain Package
```
src/main/java/com/pm/clashbenchdetectionsystem/clan/
├── Clan.java                           # @Entity
├── ClanUpdateData.java                 # Value object for updates
├── TrackedClan.java                    # @Entity — which clans we poll
├── ClanRepository.java
├── TrackedClanRepository.java
├── ClanService.java                    # @Cacheable getClan, @CacheEvict startTracking
├── ClanController.java                 # REST endpoints
├── ClanMapper.java                     # MapStruct entity→DTO mapper
└── clanDTO/
    └── ClanResponse.java              # API response DTO
```

### Player Domain Package
```
src/main/java/com/pm/clashbenchdetectionsystem/player/
├── Player.java                         # @Entity
├── PlayerUpdateData.java               # Value object for updates
├── PlayerHero.java                     # @Entity (composite key)
├── PlayerEquipment.java                # @Entity
├── PlayerTroop.java                    # @Entity
├── PlayerSpell.java                    # @Entity
├── PlayerPet.java                      # @Entity
├── PlayerRepository.java
├── PlayerService.java                  # @Cacheable for player, cwlHistory, stats
├── PlayerController.java
├── PlayerMapper.java                   # MapStruct entity→DTO mapper
└── playerDTO/
    ├── PlayerResponse.java
    ├── PlayerCwlHistoryResponse.java
    └── PlayerStatsResponse.java
```

### CWL Domain Package
```
src/main/java/com/pm/clashbenchdetectionsystem/cwl/
├── CwlSeason.java                      # @Entity
├── CwlParticipant.java                 # @Entity (with JSONB snapshot)
├── CwlParticipantId.java              # Composite key
├── CwlWar.java                        # @Entity
├── CwlWarMember.java                  # @Entity
├── CwlAttack.java                     # @Entity
├── CwlAttackId.java                   # Composite key
├── CwlSeasonRepository.java
├── CwlParticipantRepository.java
├── CwlWarRepository.java
├── CwlWarMemberRepository.java
├── CwlAttackRepository.java
├── CwlService.java                    # Sync + programmatic CacheManager eviction
├── CwlController.java
├── CwlMapper.java                     # MapStruct entity→DTO mapper
├── CwlPoller.java                     # @Scheduled CWL polling
├── StatsSnapshotHelper.java           # Build JSONB snapshot from player data
└── cwlDTO/
    ├── CwlSeasonResponse.java
    ├── CwlWarResponse.java
    └── LeaderBoardResponse.java
```

### Scoring Package
```
src/main/java/com/pm/clashbenchdetectionsystem/scoring/
├── CbdsCalculator.java                 # Pure scoring math (no dependencies)
├── CbdsScoreService.java              # Leaderboard assembly + score persistence
├── AttackScore.java                   # Value object (per-attack score breakdown)
└── SeasonScore.java                   # Value object (aggregated season totals)
```

### Resources
```
src/main/resources/
├── application.properties              # Main config (coc.api.token, spring.cache.type=redis)
└── db/migration/
    ├── V1__initial_schema.sql          # All tables
    ├── V2__relax_constraints.sql       # TH 1-20, positions 1-50
    └── V3__add_war_member_th.sql       # town_hall_level on cwl_war_member
```

### Frontend
```
frontend/
├── package.json                        # React + Vite + TanStack Query
├── vite.config.ts                      # Dev server proxy to :8080
├── tsconfig.json
└── src/
    ├── api/client.ts                   # Fetch-based API client functions
    ├── hooks/queries.ts                # TanStack Query hooks (L1 staleTime + mutation invalidation)
    ├── pages/
    │   ├── DashboardPage.tsx           # CoC-themed hero section + tracked clans list
    │   ├── ClanPage.tsx
    │   ├── PlayerPage.tsx
    │   └── ...
    └── components/                     # Reusable UI components
```

---

## Key Dependencies

| Category | Dependency | Purpose |
|----------|------------|---------|
| **Web** | `spring-boot-starter-web` | REST controllers, embedded Tomcat |
| **Web** | `spring-boot-starter-validation` | Bean validation (JSR-380) |
| **Web** | `spring-boot-starter-actuator` | Health checks, Prometheus endpoint |
| **Database** | `spring-boot-starter-data-jpa` | JPA/Hibernate 7.x, repositories |
| **Database** | `postgresql` | PostgreSQL JDBC driver |
| **Database** | `flyway-core` + `flyway-database-postgresql` | Database migrations |
| **Cache** | `spring-boot-starter-data-redis` | Redis L2 cache |
| **Cache** | `spring-boot-starter-cache` | `@Cacheable`, `@CacheEvict` support |
| **Monitoring** | `micrometer-registry-prometheus` | Prometheus metrics export |
| **API Docs** | `springdoc-openapi-starter-webmvc-ui` (2.8.6) | Swagger UI |
| **Mapping** | `mapstruct` + `mapstruct-processor` | Entity→DTO mapping |
| **Utility** | `lombok` | Reduce boilerplate (@Slf4j, @RequiredArgsConstructor) |
| **Testing** | `spring-boot-starter-test` | JUnit 5, Mockito, AssertJ |
| **Testing** | `spring-boot-testcontainers` + `testcontainers:postgresql` | Real PostgreSQL in tests |

> **Note:** The project uses `RestClient` + `@GetExchange` for the CoC API client (Spring Framework 7), NOT `WebClient`/WebFlux.

---

## Docker Compose Services

| Service | Image | Port | Purpose |
|---------|-------|------|---------|
| `postgres` | `postgres:16-alpine` | 5432 | Primary database |
| `redis` | `redis:7-alpine` | 6379 | L2 cache (LFU, 128MB max) |
| `app` | Built from Dockerfile | 8080 | Spring Boot application |
| `prometheus` | `prom/prometheus` | 9090 | Metrics collection |
| `grafana` | `grafana/grafana` | 3001→3000 | Dashboards & visualization |

---

## Key Conventions

| Pattern | Convention |
|---------|------------|
| **Package structure** | By feature (`clan/`, `player/`, `cwl/`), not by layer |
| **Base package** | `com.pm.clashbenchdetectionsystem` |
| **Entities** | JPA entities with Lombok (@Getter, @Setter, @NoArgsConstructor) |
| **DTOs** | Java records for immutability (use `Integer` not `int` for nullable API fields) |
| **DTO mapping** | MapStruct interfaces (e.g. `ClanMapper`, `PlayerMapper`, `CwlMapper`) |
| **Dependency injection** | Constructor injection via @RequiredArgsConstructor |
| **Configuration** | @ConfigurationProperties records (type-safe) |
| **HTTP client** | RestClient + @GetExchange (Spring Framework 7 declarative HTTP) |
| **Exception handling** | @RestControllerAdvice with RFC 9457 ProblemDetail + status code mapping |
| **Database migrations** | Flyway with V{n}__{description}.sql naming |
| **Caching** | Multi-tier: L1 (TanStack Query) → L2 (Redis @Cacheable) → L3 (PostgreSQL) |
| **Cache eviction** | Targeted per-key eviction via CacheManager (NOT allEntries) |
| **Monitoring** | Micrometer Timer/Counter → Prometheus → Grafana |
| **Tests** | @DataJpaTest for repositories, @SpringBootTest for integration |

---

## Reference Files

| File | Purpose |
|------|---------|
| `docs/CBDS Claude File.md` | Project overview, decisions, milestones |
| `docs/SCORING_ALGORITHM.md` | CBDS scoring formula and examples |
| `docs/API_DATA_MAPPING.md` | CoC API endpoints, Java DTOs, polling workflow |
| `docs/PROJECT_SCAFFOLD.md` | This file |
| `docker-compose.yml` | All services (PostgreSQL, Redis, app, Prometheus, Grafana) |
| `prometheus.yml` | Prometheus scrape config |
| `frontend/src/hooks/queries.ts` | TanStack Query hooks with L1 cache config |
