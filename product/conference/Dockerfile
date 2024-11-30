FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app

COPY . .

WORKDIR /app/product/conference
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/product/conference/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]