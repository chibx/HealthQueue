# ── Stage 1: Build ──────────────────────────────────────────────────────────
# Use the official Gradle+JDK image — no wrapper jar needed
FROM gradle:8.8-jdk21-alpine AS builder

WORKDIR /app

# Pass Railway Postgres credentials as build args so jOOQ can
# connect to the database and generate source code at build time
ARG JDBC_URL
ARG DB_USER
ARG DB_PASSWORD
ENV JDBC_URL=${JDBC_URL}
ENV DB_USER=${DB_USER}
ENV DB_PASSWORD=${DB_PASSWORD}

# Copy build scripts first for better layer caching
COPY gradle.properties .
COPY settings.gradle .
COPY app/build.gradle app/build.gradle

# Copy source
COPY app/src/ app/src/

# Build the jar — skip tests, use the system gradle (not wrapper)
RUN gradle :app:jar -x test -x check --no-daemon

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/app/build/libs/*.jar app.jar

# Railway injects PORT automatically; Spark reads it with default 3000
EXPOSE 3000

ENTRYPOINT ["java", "-jar", "app.jar"]
