# ===== Build =====
# Build stage runs on the builder's native platform: the jar is platform independent,
# so multi-arch images (amd64 + arm64) do not need slow emulation.
FROM --platform=$BUILDPLATFORM maven:3-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# ===== Run =====
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN addgroup -S wordmaster && adduser -S wordmaster -G wordmaster
COPY --from=build /app/target/WordMaster.jar app.jar
USER wordmaster
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -Dliquibase.analytics.enabled=false"
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q UP || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
