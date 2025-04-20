# Use Java 21 to match your pom.xml configuration
FROM eclipse-temurin:21-jre-jammy

# Create a volume for temporary files
VOLUME /tmp

# Copy the JAR file (verify the name matches your actual build output)
COPY target/task-tracker-*.jar app.jar

# Expose the port that your application runs on
EXPOSE 8080

# Start the application with PostgreSQL-friendly settings
ENTRYPOINT ["java", "-jar", "/app.jar"]