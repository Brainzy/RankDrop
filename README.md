# 🏆 RankDrop

**Self-hosted leaderboard backend for any application. Own your data, zero recurring cost.**

> Run on a free Oracle Cloud instance. No pricing per user, no vendor lock-in, no surprises.

---

## Who Is This For?

RankDrop is for developers who want a leaderboard backend without paying monthly fees or trusting third parties with their data. If your app needs score tracking, rankings, or competitive features — RankDrop runs on infrastructure you control.

Works with any HTTP client — mobile, web, desktop, or game engines.

---

## Features

- **Multiple leaderboard types** — all-time, daily, weekly, monthly with automatic resets
- **Flexible scoring** — high score wins, lowest time wins, or cumulative totals
- **Player moderation** — ban players globally, remove individual scores
- **Webhook notifications** — get notified on Discord or Slack when top scores are beaten
- **Automatic backups** — daily database backups with configurable retention
- **Production ready** — caching, connection pooling, health checks, structured logging

---

## Quick Start

```bash
git clone https://github.com/Brainzy/rankdrop.git
cd rankdrop
./mvnw spring-boot:run
```

Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

No configuration needed. H2 database starts automatically and a default leaderboard is seeded.

---

## API Overview

### Client API

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/leaderboards/{slug}/scores` | Submit a score |
| GET | `/api/v1/leaderboards/{slug}/top` | Get top N scores |
| GET | `/api/v1/leaderboards/{slug}/players/{alias}` | Get player rank and surrounding scores |

### Admin API

| Group | Description |
|---|---|
| Leaderboards | Create, configure, reset, delete |
| Players | Ban, unban, list banned players |
| Scores | View all scores, remove individual entries |
| Settings | Rotate game key, configure webhooks, backup settings |
| Archive | View reset history and archived snapshots |

Full interactive documentation at `/swagger-ui/index.html`.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 25, Spring Boot 3.x |
| Database | H2 (embedded) / PostgreSQL (optional) |
| Docs | OpenAPI 3 / Swagger UI |
| Deployment | Docker, Docker Compose, Caddy |

---

## Further Reading

- [FEATURES.md](FEATURES.md) — full breakdown of implemented and planned features
- [ARCHITECTURE.md](ARCHITECTURE.md) — technical design decisions and rationale

---

## Roadmap

**v1.0.0 (Target: Q1 2026)** — Core leaderboard, Docker deployment, Swagger documentation.

**v1.1.0 (Target: Q2 2026)** — PostgreSQL support, additional integrations.

---

## License

MIT License. See [LICENSE](LICENSE) for details.

---

Built by [Brainzy](https://github.com/Brainzy)
