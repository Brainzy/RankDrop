# RankDrop — Architecture & Design Decisions

Technical decisions and the reasoning behind them.

---

## Why Java?

Modern Spring with Lombok and records is not verbose. Maintenance cost is low — the framework handles most
cross-cutting concerns so the application code stays focused on business logic.

Spring Boot 4 with Java 25 also enables GraalVM native image compilation out of the box, producing a binary that starts
in ~50ms and runs in ~100MB RAM — competitive with Go or Rust for a deployed service.

## Why PostgreSQL?

PostgreSQL is the standard for production relational databases. It runs identically across local Docker, Google Cloud,
Koyeb, and Oracle Cloud — one SQL dialect, one migration path, no surprises. Backup and restore is `pg_dump` and
`pg_restore` on any platform.

## Why GraalVM Native Image?

The target deployment is a free-tier cloud VM with 512MB–1GB RAM shared with a PostgreSQL instance. A standard JVM
Spring Boot application idles at ~400MB — leaving almost no headroom. GraalVM native image compiles the entire
application ahead of time, producing a self-contained binary that idles at ~100MB and starts in ~50ms.

The tradeoff is build time — native compilation takes 10–15 minutes vs 30 seconds for a JVM build. This only affects
deployment, not development. Local development runs on the JVM as normal.

## Why Cache Top 100 Instead of Top N?

Caching per request limit creates key collisions — top 10 and top 50 are separate cache entries needing separate
invalidation logic. Caching a fixed top 100 means any request for top N slices the list in memory. One cache entry per
leaderboard, one invalidation point.

Invalidation is conditional — only evict when the new score would appear in top 100. This keeps the cache warm for the
majority of submissions that don't crack the top ranks. As a side effect, cached reads survive database outages since
entries are never expired automatically, only explicitly evicted.

## Why Two-Tier Authentication?

Admin token lives in an environment variable — it must exist before the application starts and should never rotate
through the app itself. Game key lives in the database — it needs to be rotatable at runtime without redeployment if
compromised. The interceptor checks the database key first, falls back to the environment variable, allowing
zero-downtime migration between the two approaches.

## Why Atomic Database Updates for Score Submission?

Score submission uses conditional UPDATE queries rather than read-modify-write cycles. For BEST_ONLY mode:

```sql
UPDATE score_entries SET score_value = :value, submitted_at = :now
WHERE leaderboard_id = :id AND player_alias = :alias AND score_value < :value
```

Two concurrent submissions for the same player cannot produce a lost update — the database handles the comparison
atomically. The rows affected count determines whether to insert a new entry or return the existing score. No optimistic
locking, no retry logic, no application-level synchronization needed.

## Why Async Webhooks?

A slow or unavailable webhook endpoint should never delay a score submission response. Firing asynchronously means the
player gets their result immediately. Failures are logged and swallowed — a notification failure is not a reason to fail
a score write.

## Why Flyway for Schema Migrations?

`spring.jpa.hibernate.ddl-auto=update` is convenient during development but dangerous in production — Hibernate can
silently drop columns or alter types. Flyway gives versioned, auditable SQL migrations that run exactly once and are
tracked in a schema history table. Every schema change is explicit, reviewable, and reproducible on a fresh database.

## Why a Settings Table?

Two categories of configuration exist — secrets needed before startup (environment variables) and operational settings
adjustable after deployment (database). The settings table covers the latter: game key rotation, webhook configuration,
backup retention. A developer can reconfigure their entire deployment through Swagger without touching the server.

## Why Gzip Compression?

Leaderboard responses are repetitive JSON — player names and scores compress extremely well. Enabling gzip at the server
level reduces egress by ~70% with zero application code changes. On free-tier hosting where egress is the primary cost
driver, this extends the free usage threshold by 3x without any tradeoffs.