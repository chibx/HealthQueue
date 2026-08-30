# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy gradle wrapper and build scripts first (better layer caching)
COPY gradlew .
COPY gradle/ gradle/
COPY gradle.properties .
COPY settings.gradle .

# Copy only build files first to cache dependency resolution
COPY app/build.gradle app/build.gradle
COPY app/src/ app/src/

RUN chmod +x gradlew

# jOOQ needs DB access at compile time to generate sources.
# Railway provides JDBC_URL, DB_USER, DB_PASSWORD as build-time env vars
# when a Postgres addon is linked. Pass them through ARG → ENV.
ARG JDBC_URL
ARG DB_USER
ARG DB_PASSWORD
ENV JDBC_URL=${JDBC_URL}
ENV DB_USER=${DB_USER}
ENV DB_PASSWORD=${DB_PASSWORD}

# Build — skip tests only. jOOQ codegen runs because generateSchemaSourceOnCompilation=true
RUN ./gradlew :app:jar -x test -x check --no-daemon --info

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/app/build/libs/*.jar app.jar

# The Spark server reads PORT env var; Railway injects it automatically
EXPOSE 3000

ENTRYPOINT ["java", "-jar", "app.jar"]
