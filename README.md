# Clash Bench Detection System (CBDS)

Track your clan's Clan War League (CWL) performance in Clash of Clans. CBDS records every attack, scores players using a custom algorithm, and produces a leaderboard so you know who's carrying their weight and who should be on the bench.

## Quick Start

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- A Clash of Clans API token from [developer.clashofclans.com](https://developer.clashofclans.com)

### 1. Get a CoC API Token

Go to [developer.clashofclans.com](https://developer.clashofclans.com), create an account, and generate an API key. **Tokens are locked to a specific IP address** — use the public IP of the machine where CBDS will run.

### 2. Configure

```bash
cp .env.example .env
```

Edit `.env` and paste your API token:

```
COC_API_TOKEN=eyJ0eXAiOiJKV1Qi...
```

### 3. Run

```bash
docker compose up -d
```

The app is now running at **http://localhost:8080**.

## Usage

1. **Track a clan** — use the dashboard search or call the API:
   ```
   POST http://localhost:8080/api/tracking/clans?clanTag=%2329GPGULQG
   ```
   (Replace `%23` with your clan's `#` tag, URL-encoded)

2. **Sync CWL data** — during an active CWL season:
   ```
   POST http://localhost:8080/api/clans/%2329GPGULQG/cwl/sync
   ```

3. **View the leaderboard** — open the app in your browser and navigate to your clan's CWL season.

## Optional: Monitoring

To add Prometheus and Grafana dashboards:

```bash
docker compose -f docker-compose.yml -f docker-compose.monitoring.yml up -d
```

- Prometheus: http://localhost:9090
- Grafana: http://localhost:3001 (default login: admin/admin)

## Development

For local development with hot-reload:

**Backend:**
```bash
docker compose up -d postgres redis
./mvnw spring-boot:run "-Dspring-boot.run.arguments=--coc.api.token=YOUR_TOKEN" "-Dspring-boot.run.profiles=dev"
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

The Vite dev server runs at http://localhost:5173 and proxies API requests to the Spring Boot backend on port 8080.

## CoC API Token Notes

- Tokens are **IP-locked** by Supercell. If your public IP changes, you'll need to regenerate the token.
- Deploying to a cloud VPS with a static IP avoids this problem entirely.
- If you see `403 Forbidden` errors in the logs, your token's IP no longer matches. Regenerate it at [developer.clashofclans.com](https://developer.clashofclans.com).

## Tech Stack

- **Backend**: Java 21, Spring Boot 4, PostgreSQL 16, Redis 7
- **Frontend**: React, Vite, TanStack Query, Tailwind CSS
- **Monitoring**: Prometheus, Grafana (optional)
