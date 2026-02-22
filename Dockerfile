# Build stage
FROM ghcr.io/graalvm/native-image-community:25 AS builder
WORKDIR /app
ENV LANG=en_US.UTF-8
ENV LC_ALL=en_US.UTF-8
COPY . .
RUN chmod +x ./mvnw && ./mvnw -Pnative native:compile -DskipTests -q

# Run stage
FROM ubuntu:22.04
WORKDIR /app
COPY --from=builder /app/target/rankdrop .
EXPOSE 8080
ENTRYPOINT ["/app/rankdrop"]