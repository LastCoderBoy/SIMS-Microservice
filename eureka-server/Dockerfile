# ========================================
# Multi-stage build for Eureka Server
# ========================================

# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy parent pom
COPY pom.xml .

# Copy ALL modules (Maven needs to see the full structure)
COPY sims-common/ sims-common/
COPY eureka-server/ eureka-server/
COPY api-gateway/ api-gateway/
COPY auth-service/ auth-service/
COPY sims-core-service/ sims-core-service/

# Build the project
RUN mvn clean package -pl eureka-server -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add non-root user
RUN addgroup --system spring && \
    adduser --system --ingroup spring spring && \
    chown -R spring:spring /app

USER spring:spring

# Copy JAR from build stage
COPY --from=build /app/eureka-server/target/*.jar app.jar

# Expose port
EXPOSE 8761

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8761/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]