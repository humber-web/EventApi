# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-alpine

# Install bash (required for the wait script)
RUN apk add --no-cache bash

# Set the working directory inside the container
WORKDIR /app

# Copy the wait script
COPY wait-for-it.sh /app/wait-for-it.sh

# Make the wait script executable
RUN chmod +x /app/wait-for-it.sh

# Copy the built JAR file into the container
COPY target/event-ticketing-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir -p /app/images
RUN mkdir -p /app/qr-codes 
# Expose the port your Spring Boot app runs on (default is 8080)
EXPOSE 8080

# Define the command to run your Spring Boot app after waiting for PostgreSQL
ENTRYPOINT ["./wait-for-it.sh", "postgres:5432", "--", "java", "-jar", "/app/app.jar"]
