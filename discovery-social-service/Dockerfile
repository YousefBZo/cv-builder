# syntax=docker/dockerfile:1.7

# Stage 1: Build & Package the Application
FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app

ARG SERVICE_NAME
ENV MAVEN_RETRY_OPTS="-Dmaven.wagon.http.retryHandler.count=5 -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 -Dhttp.keepAlive=false"

# Copy the parent and module POM files so Maven can resolve the reactor graph.
COPY pom.xml .
COPY discovery-server/pom.xml discovery-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY identity-service/pom.xml identity-service/pom.xml
COPY cv-management-service/pom.xml cv-management-service/pom.xml
COPY discovery-social-service/pom.xml discovery-social-service/pom.xml

COPY ${SERVICE_NAME}/src ${SERVICE_NAME}/src

# Package the selected module and skip unit tests to accelerate the build pipeline.
RUN --mount=type=cache,target=/root/.m2 \
    for attempt in 1 2 3; do \
        mvn -B -U -pl ${SERVICE_NAME} -am clean package -DskipTests ${MAVEN_RETRY_OPTS} && exit 0; \
        echo "Maven build failed on attempt ${attempt}; retrying..."; \
        sleep 10; \
    done; \
    exit 1

# Stage 2: Minimalist Production Runtime Environment
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

ARG SERVICE_NAME

# Extract only the compiled runner JAR artifact from the builder stage.
COPY --from=builder /app/${SERVICE_NAME}/target/*.jar app.jar

# Define the container execution entry point.
ENTRYPOINT ["java", "-jar", "app.jar"]
