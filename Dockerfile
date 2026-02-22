# Build stage
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./mvnw && ./mvnw package -DskipTests -q

# Run stage
FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]