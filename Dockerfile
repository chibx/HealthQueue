# ── Stage 1: Frontend build ───────────────────────────────────────────────
FROM node:20-alpine AS web-builder

WORKDIR /app/web

COPY web/package*.json ./
RUN npm install

COPY web/ ./
RUN npm run build

# ── Stage 2: Java build ────────────────────────────────────────────────────
FROM gradle:8.8-jdk21-alpine AS java-builder

WORKDIR /app

# Pass Railway Postgres credentials as build args for jOOQ
ARG JDBC_URL
ARG DB_USER
ARG DB_PASS
ENV JDBC_URL=${JDBC_URL}
ENV DB_USER=${DB_USER}
ENV DB_PASS=${DB_PASS}

# Copy Gradle project files
COPY gradle/ gradle/
COPY gradle.properties .
COPY settings.gradle .
COPY app/build.gradle app/build.gradle

# Copy Java source
COPY app/src/ app/src/

# Include the built frontend assets in the same folder layout expected by App.java
COPY --from=web-builder /app/web/dist ./web/dist

# Build the jar; jOOQ generation is triggered as part of the Java build
RUN gradle :app:classes :app:jar -x test -x check --no-daemon

# ── Stage 3: Runtime ───────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=java-builder /app/app/build/libs/*.jar app.jar
COPY --from=java-builder /app/web/dist ./web/dist

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "app.jar"]