# 🏆 RankDrop

**The self-hosted, Docker-optimized leaderboard service for indie games.**

## ✨ Why RankDrop?

* **Zero Recurring Cost:** Run it on a free Oracle cloud instance. No per-player or per-request pricing.
* **Embedded & Scalable:** Starts with a zero-config H2 database; migrates seamlessly to PostgreSQL when your game goes
  viral.
* **Indie Optimized:** Native support for both high-score (points) and low-score (speedrun) leaderboards.
* **Developer Experience:** Fully documented with Swagger UI. Test your endpoints directly from the browser.

## 🛠️ Tech Stack

* **Core:** Spring Boot 3.x (Java 25)
* **Persistence:** H2 (Embedded) / PostgreSQL (External)
* **Docs:** OpenAPI 3 / Swagger UI
* **Containerization:** Docker & Docker Compose

## 🗺️ Roadmap

Development is currently underway! For a full list of implemented and planned features, please
check [FEATURES.md](FEATURES.md).

## 🚀 Quick Start

### Run Locally (Development)

```bash
# Clone and run
git clone https://github.com/Brainzy/rankdrop.git
cd rankdrop
./mvnw spring-boot:run

# Access Swagger UI
open http://localhost:8080/swagger-ui/index.html
```

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

---
Built with ❤️ for the Indie Game Community by [Brainzy](https://github.com/Brainzy)