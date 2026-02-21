# RankDrop — Architecture & Design Decisions

Technical decisions and the reasoning behind them.

---

## Why Java?

Spring Boot gives a production-ready HTTP server, dependency injection, scheduling, caching, and database access out of
the box. Modern Spring with Lombok and records is not verbose. Maintenance cost is low — the framework handles most
cross-cutting concerns so the application code stays focused on business logic.

## Why H2 over SQLite?

Both are embedded, zero-configuration databases. H2 was chosen for native Java compatibility — no native binaries, no
platform-specific drivers, no JNI concerns. It starts with the JVM and requires nothing from the host system. SQLite
would have required additional bridging.

The tradeoff is write concurrency — H2 serializes writes at the table level. This is acceptable for the target
deployment scale. PostgreSQL is the migration path when concurrency becomes a bottleneck.

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

## Why Async Webhooks?

A slow or unavailable webhook endpoint should never delay a score submission response. Firing asynchronously means the
player gets their result immediately. Failures are logged and swallowed — a notification failure is not a reason to fail
a score write.

## Why H2 BACKUP Command Over File Copy?

Copying the H2 file while the database is open risks a corrupt backup captured mid-write. The `BACKUP TO` SQL command
produces a consistent snapshot regardless of concurrent activity. Retention is handled by deleting files older than the
configured threshold.

## Why a Settings Table?

Two categories of configuration exist — secrets needed before startup (environment variables) and operational settings
adjustable after deployment (database). The settings table covers the latter: game key rotation, webhook configuration,
backup retention. A developer can reconfigure their entire deployment through Swagger without touching the server.