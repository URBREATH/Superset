# ── Stage 1: Build con Maven ─────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build

# Cache layer dipendenze Maven
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Compilazione sorgenti
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: Runtime leggero ─────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia JAR compilato
COPY --from=builder /build/target/urbreath-ds-jobs-1.0.0.jar app.jar

# Directory per override configurazione (montare application.yaml custom qui)
RUN mkdir -p /app/config

# Utente non-root per sicurezza
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Nessuna porta HTTP esposta (app only-scheduler, nessun endpoint REST)
# EXPOSE 5005   # Decommentare solo per debug remoto JDWP

ENTRYPOINT ["java", \
  "-Djasypt.encryptor.password=${JASYPT_PASSWORD}", \
  "-Dspring.config.additional-location=optional:file:/app/config/", \
  "-jar", "app.jar"]

