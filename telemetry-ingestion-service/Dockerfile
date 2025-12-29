# Runtime image for Telemetry Ingestion Service
FROM amazoncorretto:17-alpine

# Set working directory
WORKDIR /app

# Copy the pre-built jar file
COPY telemetry-ingestion-service/target/*.jar app.jar

# Expose port
EXPOSE 8081

# Set JVM options for containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
