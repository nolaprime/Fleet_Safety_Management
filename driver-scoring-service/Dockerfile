# Runtime image for Event Processing Service
FROM amazoncorretto:17-alpine

# Set working directory
WORKDIR /app

# Copy the pre-built jar file
COPY driver-scoring-service/target/*.jar app.jar

# No port exposure needed (consumer service)

# Set JVM options for containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
