# ========================================
# Multi-stage build for Core Service
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

# Build
RUN mvn clean package -pl sims-core-service -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup --system spring && \
    adduser --system --ingroup spring spring && \
    chown -R spring:spring /app

USER spring:spring

COPY --from=build /app/sims-core-service/target/*.jar app.jar

EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=3s --start-period=90s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]