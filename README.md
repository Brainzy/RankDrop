# 🏆 RankDrop

**Self-hosted leaderboard backend for any application. Own your data, zero recurring cost.**

> Deploy on Google Cloud, Koyeb, or Oracle Cloud free tier. No pricing per user, no vendor lock-in, no surprises.

---

## Who Is This For?

RankDrop is for developers who want a leaderboard backend without paying monthly fees or trusting third parties with
their data. If your app needs score tracking, rankings, or competitive features — RankDrop runs on infrastructure you
control.

Works with any HTTP client — mobile, web, desktop, or game engines.

---

## Features

- **Multiple leaderboard types** — all-time, daily, weekly, monthly with automatic resets
- **Flexible scoring** — high score wins, lowest time wins, or cumulative totals
- **Concurrent safe** — atomic writes prevent lost updates under load
- **Player moderation** — ban players globally, remove individual scores
- **Webhook notifications** — get notified on Discord or Slack when top scores are beaten
- **Automatic backups** — daily database backups with configurable retention
- **Production ready** — caching, connection pooling, health checks, structured logging
- **Tiny footprint** — GraalVM native image, ~118MB Docker image, ~50ms startup

---

## Quick Start

Requires Docker.

```bash
git clone https://github.com/Brainzy/rankdrop.git
cd rankdrop
cp .env.example .env   # fill in your secrets
docker compose up
```

Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## API Overview

### Client API

| Method | Endpoint                                      | Description                                    |
|--------|-----------------------------------------------|------------------------------------------------|
| POST   | `/api/v1/leaderboards/{slug}/scores`          | Submit a score                                 |
| GET    | `/api/v1/leaderboards/{slug}/top`             | Get top N scores                               |
| GET    | `/api/v1/leaderboards/{slug}/players/{alias}` | Get player rank and surrounding scores         |
| GET    | `/api/v1/leaderboards/{slug}/context`         | Get top scores + player context in one request |

### Admin API

| Group        | Description                                          |
|--------------|------------------------------------------------------|
| Leaderboards | Create, configure, reset, delete                     |
| Players      | Ban, unban, list banned players                      |
| Scores       | View all scores, remove individual entries           |
| Settings     | Rotate game key, configure webhooks, backup settings |
| Archive      | View reset history and archived snapshots            |

Full interactive documentation at `/swagger-ui/index.html`.

---

## Tech Stack

| Layer      | Technology                                   |
|------------|----------------------------------------------|
| Runtime    | Java 25, Spring Boot 4.x                     |
| Database   | PostgreSQL                                   |
| Migrations | Flyway                                       |
| Docs       | OpenAPI 3 / Swagger UI                       |
| Deployment | Docker, Docker Compose, GraalVM Native Image |

---

## Hosting

RankDrop is designed to run free forever on:

- **[Google Cloud Free Tier](docs/deploy-gcp.md)** — limited by 1GB egress
- **[Koyeb + Aiven](docs/deploy-koyeb.md)** — no card required, sleeps after 60 min of no interaction for about 3
  seconds
- **[Oracle Cloud Free Tier](docs/deploy-oracle.md)** — painful signup

> Hosting providers control their own pricing and free tier terms. RankDrop itself is always free and open source.

---

## Further Reading

- [FEATURES.md](FEATURES.md) — full breakdown of implemented and planned features
- [ARCHITECTURE.md](ARCHITECTURE.md) — technical design decisions and rationale

---

## License

Apache 2.0 License. See [LICENSE](LICENSE) for details.

The deployment tooling and Unity SDK are proprietary and not covered by this license.

---

Built by [Brainzy](https://github.com/Brainzy)