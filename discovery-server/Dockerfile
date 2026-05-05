FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /build
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY discovery-server/pom.xml discovery-server/
RUN ./mvnw dependency:go-offline -pl discovery-server -am -q
COPY discovery-server/src discovery-server/src
RUN ./mvnw package -pl discovery-server -am -DskipTests -q

FROM eclipse-temurin:17-jre-alpine AS runtime
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=builder /build/discovery-server/target/*.jar app.jar
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://eureka:eureka-secret@localhost:8761/actuator/health || exit 1
USER spring:spring
EXPOSE 8761
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
