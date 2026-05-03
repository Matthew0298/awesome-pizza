# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY awesome-pizza/pom.xml ./pom.xml
COPY awesome-pizza/.mvn ./.mvn
COPY awesome-pizza/mvnw ./mvnw
COPY awesome-pizza/src ./src

RUN chmod +x mvnw && ./mvnw -q -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
