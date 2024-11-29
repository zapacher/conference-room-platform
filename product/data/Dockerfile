FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app

COPY . .

WORKDIR /app/product/data
RUN mvn clean install -DskipTests

FROM openjdk:17-jdk-alpine
WORKDIR /app

COPY --from=build /app/target/data-1.0-SNAPSHOT.jar /app/
