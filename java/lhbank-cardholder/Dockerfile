# Use OpenJDK as base image
FROM eclipse-temurin:21-jdk-jammy

# Set a working directory inside the container
WORKDIR /app

# Copy the Spring Boot JAR
COPY target/lhbank-cardholder-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on (default 8080)
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java","-jar","app.jar"]