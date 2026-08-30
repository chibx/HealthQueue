# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM gradle:8.8-jdk21-alpine AS builder

WORKDIR /app

# Pass Railway Postgres credentials as build args for jOOQ
ARG JDBC_URL
ARG DB_USER
ARG DB_PASSWORD
ENV JDBC_URL=${JDBC_URL}
ENV DB_USER=${DB_USER}
ENV DB_PASSWORD=${DB_PASSWORD}

# ISSUES 1 & 2: Copy the gradle directory so libs.versions.toml is available
COPY gradle/ gradle/
COPY gradle.properties .
COPY settings.gradle .
COPY app/build.gradle app/build.gradle

COPY app/src/ app/src/

# ISSUE 3: Explicitly run 'classes' before 'jar' to guarantee jOOQ codegen triggers
RUN gradle :app:classes :app:jar -x test -x check --no-daemon

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/app/build/libs/*.jar app.jar

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "app.jar"]