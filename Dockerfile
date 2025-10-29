# Build per ottenere il jar
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Copio i file necessari per la build
COPY pom.xml .
COPY src ./src

# Compilo e creo il jar
RUN mvn clean package -DskipTests

# Runtime con immagine più leggera (solo la JDK)
FROM openjdk:21-jdk-slim
WORKDIR /app

# Prendo il file jar dalla build
COPY --from=builder /build/target/authentication-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8443
ENTRYPOINT ["java", "-jar", "/app.jar"]