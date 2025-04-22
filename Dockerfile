# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Expose port - Note: This is just documentation, actually uses PORT env var
EXPOSE 8080

# Set memory options for JVM in container
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Start with dynamic port binding from environment variable
CMD java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:render} -jar app.jar