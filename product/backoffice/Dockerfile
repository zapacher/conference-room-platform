FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app

COPY . .

WORKDIR /app/product
RUN mvn clean install

WORKDIR /app/product/backoffice
RUN mvn clean package

FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/product/backoffice/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]