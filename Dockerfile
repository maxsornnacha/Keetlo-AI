# ---- Build stage: Maven + JDK ----
FROM maven:3.9-eclipse-temurin-24 AS build
WORKDIR /app

# Cache deps first
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Build
COPY src ./src
RUN mvn -B -DskipTests clean package

# ---- Runtime stage: small JRE ----
FROM eclipse-temurin:24-jre
WORKDIR /app

# Default port (can override with -e SERVER_PORT=xxxx)
ENV SERVER_PORT=3016 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75" \
    SPRING_PROFILES_ACTIVE=prod \
    TZ=UTC

# Copy fat JAR from build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Non-root user
RUN useradd -r -u 1001 spring && chown -R spring /app
USER spring

# Document the container port
EXPOSE 3016

# Optional healthcheck (requires actuator)
# HEALTHCHECK --interval=30s --timeout=3s --retries=5 \
#   CMD wget -qO- http://localhost:${SERVER_PORT}/actuator/health | grep UP || exit 1

# Pass server.port and any extra JVM opts
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dserver.port=${SERVER_PORT} -jar /app/app.jar"]
