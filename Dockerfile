# Use standard OpenJDK image instead of Eclipse Temurin
FROM openjdk:21-slim

# Create a volume for temporary files
VOLUME /tmp

# Copy the JAR file
COPY target/task-tracker-*.jar app.jar

# Expose the port that your application runs on
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]