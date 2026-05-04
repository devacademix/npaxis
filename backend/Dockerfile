## Multi-stage build for production-grade Spring Boot application
## Stage 1: Build
#FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
#
#WORKDIR /app
#
## Copy pom.xml and download dependencies first (better layer caching)
#COPY pom.xml .
#RUN mvn dependency:go-offline -B
#
## Copy source code
#COPY src ./src
#
## Build the application (skip tests for faster build)
#RUN mvn clean package -DskipTests
#
## Stage 2: Runtime
#FROM eclipse-temurin:21-jre-alpine
#
#LABEL maintainer="NPaxis Team"
#LABEL description="NPaxis Application - Spring Boot 4.0 with Java 21"
#
## Create non-root user for security
#RUN addgroup -S npaxis && adduser -S npaxis -G npaxis
#
## Create log directory
#RUN mkdir -p /var/log/npaxis && chown -R npaxis:npaxis /var/log/npaxis
#
## Install curl for health checks
#RUN apk add --no-cache curl
#
#WORKDIR /app
#
## Copy JAR from builder stage
#COPY --from=builder /app/target/*.jar ./npaxis.jar
#
## Copy application configuration
#COPY --from=builder /app/src/main/resources/application.yml ./config/
#COPY --from=builder /app/src/main/resources/logback-spring.xml ./config/
#
## Change ownership
#RUN chown -R npaxis:npaxis /app
#
## Switch to non-root user
#USER npaxis
#
## Expose port (will be overridden by PORT env var on DigitalOcean)
#EXPOSE 8080
#
## Health check
#HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
#    CMD curl -f http://localhost:8080/api/v1/actuator/health/live || exit 1
#
## JVM tuning for containerized deployment
#ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+UnlockExperimentalVMOptions -XX:G1NewCollectionPercentage=30 -XX:G1MaxNewGenPercent=40 -XX:InitiatingHeapOccupancyPercent=35"
#
## Start application
#ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar npaxis.jar --spring.config.location=file:./config/"]
