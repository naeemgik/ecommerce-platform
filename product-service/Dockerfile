# Multi-stage build for optimized image
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and POMs first (leverage Docker cache layer)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY product-service/pom.xml product-service/

# Download dependencies (cached if pom.xml unchanged)
RUN ./mvnw dependency:go-offline -pl product-service -am -q

# Copy source and build
COPY product-service/src product-service/src
RUN ./mvnw package -pl product-service -am -DskipTests -q

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-alpine AS runtime

# Security: non-root user
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# JVM tuning for containerized environments
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+ExitOnOutOfMemoryError \
               -Djava.security.egd=file:/dev/./urandom"

COPY --from=builder /build/product-service/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1

USER spring:spring

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
