# Clash-Bench-Detection-System
Do you play Clash of Clans(CoC)? Are you in a clan that wants to take it to the next level? This application tracks performance for members participating in Clan War League(CWL). It will analyze attack data to crown top performers and point out the weakest links(the next one to be benched) using a custom scoring algorithm, **Clash Bench Detection System(CBDS)**. The algorithm is fine tuned to weigh attacks based on several factors, not just raw stars and destruction.

> **"Who's carrying their weight, and who should be on the bench?"**

## Features

- **CWL Performance Tracking** — Records all attacks with stars, destruction %, and positional data
- **CBDS Scoring** — Fair evaluation that rewards difficult attacks and penalizes easy misses
- **Season Leaderboards** — Rank players by performance, not just participation
- **Player Profiles** — Historical CWL stats and trends
- **Real-Time Polling** — Captures attack data during active wars before it's lost

---

## Tech Stack

| Layer | Technology | Purpose |
|-------|------------|---------|
| **Backend** | Java 21, Spring Boot 3.4 | REST API, scheduling, business logic |
| **Database** | PostgreSQL 16 | Primary data store with JSONB for snapshots |
| **Cache** | Redis 7 | Write-through caching with LFU eviction |
| **Migrations** | Flyway | Version-controlled schema management |
| **External API** | Clash of Clans API | Source of clan, player, and war data |
| **Containerization** | Docker, Docker Compose | Local development and deployment |

---
