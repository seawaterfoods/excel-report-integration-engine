# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -q -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN mkdir -p /data/import /data/output /app/config /app/logs

COPY --from=build /app/target/*.jar app.jar
COPY config/application.yml /app/config/application.yml

ENV SPRING_CONFIG_LOCATION=file:/app/config/application.yml

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/app/config/application.yml"]
