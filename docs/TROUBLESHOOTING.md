# Troubleshooting Guide - Fleet Management System

## Common Issues and Solutions

---

## Kafka Issues

### Issue: "Connection refused" to localhost:9092

**Symptoms:**
```
org.apache.kafka.common.errors.TimeoutException: Failed to update metadata after 60000 ms.
```

**Solutions:**

1. **Check if Kafka is running:**
```bash
# For Docker:
docker ps | grep kafka

# For Homebrew:
brew services list | grep kafka

# Check port availability:
lsof -i :9092
```

2. **Restart Kafka:**
```bash
# Docker:
cd docker
docker-compose restart kafka

# Homebrew:
brew services restart kafka
```

3. **Check Kafka logs:**
```bash
# Docker:
docker logs kafka

# Homebrew:
tail -f /opt/homebrew/var/log/kafka/server.log
```

---

### Issue: Consumer lag increasing

**Symptoms:**
- Slow event processing
- Messages piling up in topics

**Diagnosis:**
```bash
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --group event-processing-group
```

**Solutions:**

1. **Increase consumer instances (concurrency):**
```yaml
# application.yml
spring:
  kafka:
    listener:
      concurrency: 5  # Increase from 3
```

2. **Increase partition count:**
```bash
kafka-topics --alter \
  --topic raw_telemetry \
  --partitions 10 \
  --bootstrap-server localhost:9092
```

3. **Check for slow processing:**
- Add logging to identify bottlenecks
- Profile database queries
- Optimize business logic

---

### Issue: Messages not being consumed

**Check:**

1. **Consumer group exists:**
```bash
kafka-consumer-groups --list --bootstrap-server localhost:9092
```

2. **Topic has messages:**
```bash
kafka-run-class kafka.tools.GetOffsetShell \
  --broker-list localhost:9092 \
  --topic raw_telemetry
```

3. **Consumer is subscribed:**
```java
// Add debug logging
log.info("Consumer subscribed to topics: {}", 
    kafkaListenerEndpointRegistry.getListenerContainerIds());
```

---

## Database Issues

### Issue: "Connection refused" to PostgreSQL

**Solutions:**

1. **Check PostgreSQL is running:**
```bash
# Docker:
docker ps | grep postgres

# macOS:
pg_isready -h localhost -p 5432
```

2. **Check connection settings:**
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fleet_management
    username: fleet_user
    password: fleet_password
```

3. **Test connection:**
```bash
psql -h localhost -U fleet_user -d fleet_management
```

---

### Issue: "Table does not exist"

**Solutions:**

1. **Run database migration:**
```bash
cd docker
docker exec -i fleet-postgres psql -U fleet_user -d fleet_management < init-db.sql
```

2. **Check Flyway migrations:**
```bash
# If using Flyway
cd driver-scoring-service
mvn flyway:info
mvn flyway:migrate
```

3. **Manually create tables:**
```bash
docker exec -it fleet-postgres psql -U fleet_user -d fleet_management -f /docker-entrypoint-initdb.d/init-db.sql
```

---

### Issue: Slow database queries

**Diagnosis:**
```sql
-- Check slow queries
SELECT query, mean_exec_time, calls 
FROM pg_stat_statements 
ORDER BY mean_exec_time DESC 
LIMIT 10;

-- Check missing indexes
SELECT schemaname, tablename, attname, n_distinct
FROM pg_stats
WHERE schemaname = 'public' 
AND n_distinct > 100;
```

**Solutions:**

1. **Add indexes:**
```sql
CREATE INDEX idx_trips_driver_start ON trips(driver_id, start_time DESC);
CREATE INDEX idx_violations_timestamp ON violations(timestamp DESC);
```

2. **Analyze tables:**
```sql
ANALYZE trips;
ANALYZE violations;
ANALYZE drivers;
```

---

## Service Issues

### Issue: Service fails to start

**Check application logs:**
```bash
# View logs
cd telemetry-ingestion-service
mvn spring-boot:run

# Look for errors in startup
```

**Common causes:**

1. **Port already in use:**
```bash
# Check what's using the port
lsof -i :8081

# Kill the process
kill -9 <PID>

# Or change the port in application.yml
```

2. **Missing dependencies:**
```bash
# Rebuild
mvn clean install

# Update dependencies
mvn dependency:resolve
```

3. **Configuration errors:**
- Check application.yml syntax
- Verify Kafka bootstrap servers
- Verify database URL

---

### Issue: API returns 500 Internal Server Error

**Debug steps:**

1. **Check application logs:**
```bash
tail -f logs/application.log
```

2. **Enable debug logging:**
```yaml
logging:
  level:
    com.fleet: DEBUG
    org.springframework.web: DEBUG
```

3. **Check stack trace:**
- Look at the exception type
- Check the root cause
- Verify input data format

---

### Issue: Telemetry validation fails

**Common validation errors:**

```json
{
  "status": "error",
  "errors": [
    {"field": "gps_lat", "error": "must be between -90 and 90"}
  ]
}
```

**Solutions:**

1. **Check payload format:**
```bash
# Valid example
curl -X POST http://localhost:8081/api/telemetry \
  -H "Content-Type: application/json" \
  -d @docs/sample-telemetry.json
```

2. **Verify field names:**
- Use snake_case: `gps_lat` not `gpsLat`
- Check required fields
- Verify data types

---

## Data Flow Issues

### Issue: Telemetry sent but no events generated

**Debugging:**

1. **Check raw_telemetry topic:**
```bash
kafka-console-consumer \
  --topic raw_telemetry \
  --from-beginning \
  --bootstrap-server localhost:9092 \
  --max-messages 5
```

2. **Check normalized_telemetry topic:**
```bash
kafka-console-consumer \
  --topic normalized_telemetry \
  --from-beginning \
  --bootstrap-server localhost:9092 \
  --max-messages 5
```

3. **Check driving_events topic:**
```bash
kafka-console-consumer \
  --topic driving_events \
  --from-beginning \
  --bootstrap-server localhost:9092
```

4. **Verify business rules:**
- Is speed actually exceeding limit?
- Is acceleration beyond threshold?
- Check rule logic in logs

---

### Issue: Trip not ending

**Check:**

1. **Active trips in memory:**
```java
// Add endpoint to check active trips
@GetMapping("/debug/active-trips")
public Map<String, ActiveTrip> getActiveTrips() {
    return tripStateMachine.getAllActiveTrips();
}
```

2. **Verify ignition status:**
- Must send `ignition_status: "OFF"` to end trip
- Case-sensitive: use "ON" or "OFF", not "on"/"off"

3. **Check database:**
```sql
SELECT * FROM trips WHERE status = 'ACTIVE';
```

---

### Issue: Driver score not updating

**Debug:**

1. **Check violations table:**
```sql
SELECT * FROM violations 
WHERE driver_id = 'DRV-ABC-123' 
ORDER BY timestamp DESC;
```

2. **Check driver record:**
```sql
SELECT * FROM drivers WHERE id = 'DRV-ABC-123';
```

3. **Verify event consumption:**
```bash
# Check consumer group lag
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --group driver-scoring-group
```

---

## Docker Issues

### Issue: Docker containers won't start

**Solutions:**

1. **Check Docker Desktop is running:**
```bash
docker info
```

2. **Check for port conflicts:**
```bash
# Find what's using the ports
lsof -i :9092  # Kafka
lsof -i :5432  # PostgreSQL
lsof -i :8080  # Kafka UI
```

3. **Clean and restart:**
```bash
cd docker
docker-compose down -v
docker-compose up -d
```

4. **Check logs:**
```bash
docker-compose logs kafka
docker-compose logs postgres
```

---

### Issue: Docker out of memory

**Symptoms:**
- Services crashing
- Docker Desktop using excessive RAM

**Solutions:**

1. **Increase Docker memory:**
- Open Docker Desktop → Settings → Resources
- Increase Memory to at least 4GB

2. **Prune unused resources:**
```bash
docker system prune -a
docker volume prune
```

---

## Maven Build Issues

### Issue: Build fails with "Cannot resolve dependencies"

**Solutions:**

1. **Update dependencies:**
```bash
mvn clean install -U
```

2. **Clear Maven cache:**
```bash
rm -rf ~/.m2/repository
mvn clean install
```

3. **Check Maven settings:**
```bash
mvn --version
cat ~/.m2/settings.xml
```

---

### Issue: Tests failing

**Debug:**

1. **Run specific test:**
```bash
mvn test -Dtest=TelemetryControllerTest
```

2. **Skip tests temporarily:**
```bash
mvn clean install -DskipTests
```

3. **Check test logs:**
```bash
cat target/surefire-reports/*.txt
```

---

## Performance Issues

### Issue: High CPU usage

**Check:**

1. **Which service is consuming CPU:**
```bash
top -o cpu
```

2. **Profile with VisualVM:**
- Connect to Spring Boot app
- Monitor CPU usage
- Identify hot methods

3. **Common causes:**
- Infinite loops in business logic
- Inefficient database queries
- Too many Kafka consumers

---

### Issue: High memory usage

**Solutions:**

1. **Increase JVM heap:**
```bash
export MAVEN_OPTS="-Xmx2g -Xms512m"
mvn spring-boot:run
```

2. **Monitor with JConsole:**
```bash
jconsole
```

3. **Check for memory leaks:**
- Use heap dump analysis
- Check for unclosed resources

---

## Network Issues

### Issue: Services can't communicate

**Check:**

1. **All services running:**
```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

2. **Kafka connectivity:**
```bash
telnet localhost 9092
```

3. **Database connectivity:**
```bash
pg_isready -h localhost -p 5432
```

---

## Getting Help

### Enable Debug Mode

```yaml
# application.yml
logging:
  level:
    root: INFO
    com.fleet: DEBUG
    org.springframework.kafka: DEBUG
    org.hibernate.SQL: DEBUG
```

### Collect Diagnostic Information

```bash
# System info
java -version
mvn -version
docker version

# Service status
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health

# Kafka status
kafka-topics --list --bootstrap-server localhost:9092

# Database status
psql -h localhost -U fleet_user -d fleet_management -c "SELECT version();"
```

### Ask for Help

When reporting issues, include:
1. Error messages and stack traces
2. Service logs
3. Diagnostic information above
4. Steps to reproduce
5. What you've already tried

---

## Reset Everything

**Nuclear option - fresh start:**

```bash
# Stop all services
pkill -f "spring-boot"

# Stop and remove Docker containers
cd docker
docker-compose down -v

# Clean Maven
cd ..
mvn clean

# Delete Kafka data (if using local Kafka)
rm -rf /tmp/kafka-logs
rm -rf /tmp/zookeeper

# Restart Docker services
cd docker
docker-compose up -d

# Wait for services to start
sleep 30

# Rebuild and run
cd ..
mvn clean install
```

Then start services one by one and test.
