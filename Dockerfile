# -------- Build stage --------
FROM ghcr.io/graalvm/native-image-community:25 AS builder

WORKDIR /app
ENV LANG=en_US.UTF-8
ENV LC_ALL=en_US.UTF-8

COPY . .
RUN chmod +x ./mvnw && ./mvnw -Pnative native:compile -DskipTests -q


# -------- Runtime stage --------
FROM ubuntu:22.04

WORKDIR /app

# Install pg_dump + curl (for healthcheck)
RUN apt-get update && \
    apt-get install -y postgresql-client curl && \
    rm -rf /var/lib/apt/lists/*

# Create backup directory
RUN mkdir -p /app/backups

# Copy native binary
COPY --from=builder /app/target/rankdrop /app/rankdrop

EXPOSE 8080

ENTRYPOINT ["/app/rankdrop"]