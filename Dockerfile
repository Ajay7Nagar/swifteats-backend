# syntax=docker/dockerfile:1
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

ARG JAR_FILE=build/libs/swifteats-backend-0.1.0-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
