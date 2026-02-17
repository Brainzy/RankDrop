# 🏆 RankDrop

**Self hosted Docker-optimized leaderboard service.**
> **Own your database without running costs**

## ✨ Why RankDrop?

* **Zero Recurring Cost:** Run it on a free Oracle cloud instance. No pricing per user or similar.
* **Embedded & Scalable:** Starts with a zero-config H2 database; migrates seamlessly to PostgreSQL when needed.
* **Supports multiple leaderboards, best score being lowest or highest, optional cumulative scores, one or multiple entry per user.
* **Developer Experience:** Fully documented with Swagger UI. Test your endpoints directly from the browser.

## 🛠️ Tech Stack

* **Core:** Spring Boot 3.x (Java 25)
* **Persistence:** H2 (Embedded) / PostgreSQL (External)
* **Docs:** OpenAPI 3 / Swagger UI
* **Containerization:** Docker & Docker Compose

## Full API spec available: [rankdrop-api-spec.json](api-docs/rankdrop-api-spec.json)

## 🗺️ Roadmap

Development is currently underway! For a full list of implemented and planned features, please
check [FEATURES.md](FEATURES.md).

## 🚀 Quick Start

### Run Locally (Development)

```bash
# 1. Clone the repository
git clone [https://github.com/Brainzy/rankdrop.git](https://github.com/Brainzy/rankdrop.git)

# 2. Enter the directory
cd rankdrop

# 3. Run using the Maven wrapper
./mvnw spring-boot:run

Access Swagger UI, once the service is running, open your browser to:

👉 http://localhost:8080/swagger-ui/index.html

Use this interface to interactively create leaderboards, submit scores, and test functionality without writing a single line of client code.

```

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

---
Built [Brainzy](https://github.com/Brainzy)
