FROM openjdk:21-jdk as builder

WORKDIR /workspace

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw package -DskipTests

FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=builder /workspace/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]