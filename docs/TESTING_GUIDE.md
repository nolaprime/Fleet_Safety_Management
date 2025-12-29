# Testing Guide - Fleet Management System

## Overview

This guide provides comprehensive testing strategies for the Fleet Management System, covering unit tests, integration tests, and end-to-end testing scenarios.

---

## Testing Pyramid

```
         /\
        /E2E\         End-to-End Tests (Few)
       /------\
      /  Int   \      Integration Tests (Some)
     /----------\
    /   Unit     \    Unit Tests (Many)
   /--------------\
```

---

## 1. Unit Testing

### Service 1: Telemetry Ingestion

#### Test Validation Logic

```java
@Test
void shouldAcceptValidTelemetry() {
    TelemetryPayload payload = createValidPayload();
    
    Set<ConstraintViolation<TelemetryPayload>> violations = 
        validator.validate(payload);
    
    assertTrue(violations.isEmpty());
}

@Test
void shouldRejectInvalidLatitude() {
    TelemetryPayload payload = createValidPayload();
    payload.setGpsLat(95.0); // Invalid
    
    Set<ConstraintViolation<TelemetryPayload>> violations = 
        validator.validate(payload);
    
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("gpsLat")));
}
```

#### Test Kafka Producer

```java
@SpringBootTest
@EmbeddedKafka(topics = {"raw_telemetry"})
class KafkaProducerServiceTest {
    
    @Autowired
    private KafkaProducerService producerService;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Test
    void shouldPublishToRawTelemetry() throws Exception {
        TelemetryPayload payload = createValidPayload();
        
        producerService.sendTelemetry(payload);
        
        // Wait for async operation
        Thread.sleep(1000);
        
        // Verify message sent
        // Use @KafkaListener in test to consume and verify
    }
}
```

### Service 2: Event Processing

#### Test Normalization Logic

```java
@Test
void shouldNormalizeCoordinates() {
    TelemetryPayload raw = TelemetryPayload.builder()
        .gpsLat(34.052234567)
        .gpsLon(-118.243728912)
        .build();
    
    NormalizedTelemetry normalized = normalizationService.normalize(raw);
    
    assertEquals(34.052235, normalized.getGpsLat(), 0.000001);
    assertEquals(-118.243729, normalized.getGpsLon(), 0.000001);
}

@Test
void shouldDiscardInsaneSpeed() {
    TelemetryPayload raw = createValidPayload();
    raw.setSpeedInstant(200.0); // Impossible
    
    NormalizedTelemetry result = normalizationService.normalize(raw);
    
    assertNull(result); // Should be discarded
}
```

#### Test Business Rules

```java
@Test
void shouldDetectSpeeding() {
    TelemetryPayload telemetry = TelemetryPayload.builder()
        .speedInstant(80.0)
        .gpsLat(34.0522)
        .gpsLon(-118.2437)
        .build();
    
    // Mock speed limit service to return 55
    when(speedLimitService.getSpeedLimit(anyDouble(), anyDouble()))
        .thenReturn(55.0);
    
    Optional<DrivingEvent> event = speedingRule.evaluate(telemetry);
    
    assertTrue(event.isPresent());
    assertEquals(EventType.SPEEDING, event.get().getEventType());
    assertEquals(Severity.HIGH, event.get().getSeverity());
}

@Test
void shouldDetectHarshBraking() {
    TelemetryPayload telemetry = TelemetryPayload.builder()
        .accelerationY(-0.45)
        .speedInstant(65.0)
        .build();
    
    Optional<DrivingEvent> event = harshBrakingRule.evaluate(telemetry);
    
    assertTrue(event.isPresent());
    assertEquals(EventType.HARSH_BRAKING, event.get().getEventType());
}

@Test
void shouldNotDetectEventWhenBelowThreshold() {
    TelemetryPayload telemetry = TelemetryPayload.builder()
        .accelerationY(-0.2) // Below threshold
        .speedInstant(65.0)
        .build();
    
    Optional<DrivingEvent> event = harshBrakingRule.evaluate(telemetry);
    
    assertFalse(event.isPresent());
}
```

### Service 3: Driver Scoring

#### Test Trip State Machine

```java
@Test
void shouldStartTripOnIgnitionOn() {
    NormalizedTelemetry telemetry = NormalizedTelemetry.builder()
        .driverId("DRV-TEST-001")
        .ignitionStatus("ON")
        .gpsLat(34.0522)
        .gpsLon(-118.2437)
        .timestamp(Instant.now())
        .build();
    
    tripStateMachine.processNormalizedTelemetry(telemetry);
    
    assertTrue(tripStateMachine.hasActiveTrip("DRV-TEST-001"));
}

@Test
void shouldCalculateDistanceDuringTrip() {
    String driverId = "DRV-TEST-001";
    
    // Start trip
    processIgnitionOn(driverId, 34.0522, -118.2437);
    
    // Move 1 mile north
    processMovement(driverId, 34.0667, -118.2437);
    
    ActiveTrip trip = tripStateMachine.getActiveTrip(driverId);
    assertThat(trip.getTotalMiles()).isCloseTo(1.0, within(0.1));
}

@Test
void shouldEndTripOnIgnitionOff() {
    String driverId = "DRV-TEST-001";
    
    // Start and move
    processIgnitionOn(driverId, 34.0522, -118.2437);
    processMovement(driverId, 34.0667, -118.2437);
    
    // End trip
    processIgnitionOff(driverId, 34.0667, -118.2437);
    
    assertFalse(tripStateMachine.hasActiveTrip(driverId));
    verify(tripRepository).save(any(Trip.class));
}
```

#### Test Haversine Distance

```java
@Test
void shouldCalculateDistanceCorrectly() {
    // Los Angeles to Pasadena (approximately 10 miles)
    double distance = GeoUtil.calculateDistance(
        34.0522, -118.2437,  // LA
        34.1478, -118.1445   // Pasadena
    );
    
    assertThat(distance).isBetween(9.5, 10.5);
}

@Test
void shouldReturnZeroForSameLocation() {
    double distance = GeoUtil.calculateDistance(
        34.0522, -118.2437,
        34.0522, -118.2437
    );
    
    assertEquals(0.0, distance, 0.001);
}
```

#### Test Scoring Engine

```java
@Test
void shouldDeductPointsForViolation() {
    DrivingEvent event = DrivingEvent.builder()
        .driverId("DRV-TEST-001")
        .eventType(EventType.HARSH_BRAKING)
        .severity(Severity.HIGH)
        .build();
    
    // Driver starts with 100 points
    Driver driver = createDriver("DRV-TEST-001", 100);
    when(driverRepository.findById("DRV-TEST-001"))
        .thenReturn(Optional.of(driver));
    
    scoringEngine.processDrivingEvent(event);
    
    ArgumentCaptor<Driver> captor = ArgumentCaptor.forClass(Driver.class);
    verify(driverRepository).save(captor.capture());
    
    assertEquals(90, captor.getValue().getCurrentScore()); // -10 for HIGH
}

@Test
void shouldNotGoBelowZeroScore() {
    Driver driver = createDriver("DRV-TEST-001", 5);
    when(driverRepository.findById("DRV-TEST-001"))
        .thenReturn(Optional.of(driver));
    
    DrivingEvent event = DrivingEvent.builder()
        .driverId("DRV-TEST-001")
        .eventType(EventType.GEOFENCE_VIOLATION)
        .severity(Severity.HIGH)
        .build(); // Would deduct 15
    
    scoringEngine.processDrivingEvent(event);
    
    ArgumentCaptor<Driver> captor = ArgumentCaptor.forClass(Driver.class);
    verify(driverRepository).save(captor.capture());
    
    assertEquals(0, captor.getValue().getCurrentScore()); // Capped at 0
}
```

---

## 2. Integration Testing

### Test with Embedded Kafka

```java
@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
    topics = {"raw_telemetry", "normalized_telemetry", "driving_events"}
)
class EventProcessingIntegrationTest {
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private CountDownLatch latch;
    
    @KafkaListener(topics = "normalized_telemetry", groupId = "test")
    public void receiveNormalized(String message) {
        latch.countDown();
    }
    
    @Test
    void shouldProcessEndToEnd() throws Exception {
        latch = new CountDownLatch(1);
        
        TelemetryPayload payload = createValidPayload();
        String json = objectMapper.writeValueAsString(payload);
        
        kafkaTemplate.send("raw_telemetry", payload.getDeviceId(), json);
        
        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }
}
```

### Test with TestContainers

```java
@SpringBootTest
@Testcontainers
class DriverScoringIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
        .withDatabaseName("test_fleet")
        .withUsername("test")
        .withPassword("test");
    
    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));
    
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
    
    @Test
    void shouldPersistTripToDatabase() {
        // Test implementation
    }
}
```

---

## 3. End-to-End Testing

### Manual Testing Script

```bash
#!/bin/bash

echo "=== Fleet Management System E2E Test ==="

# 1. Start trip (ignition ON)
echo "1. Starting trip..."
curl -X POST http://localhost:8081/api/telemetry \
  -H "Content-Type: application/json" \
  -d '{
    "device_id": "DEV-E2E-001",
    "truck_id": "TRUCK-E2E-001",
    "driver_token": "DRV-E2E-001",
    "gps_lat": 34.0522,
    "gps_lon": -118.2437,
    "speed_instant": 0,
    "engine_rpm": 800,
    "acceleration_x": 0,
    "acceleration_y": 0,
    "braking_status": false,
    "fatigue_detected": false,
    "ignition_status": "ON",
    "fuel_level": 100.0
  }'

sleep 2

# 2. Drive normally
echo "2. Driving normally..."
curl -X POST http://localhost:8081/api/telemetry \
  -H "Content-Type: application/json" \
  -d '{
    "device_id": "DEV-E2E-001",
    "truck_id": "TRUCK-E2E-001",
    "driver_token": "DRV-E2E-001",
    "gps_lat": 34.0622,
    "gps_lon": -118.2437,
    "speed_instant": 55,
    "engine_rpm": 2000,
    "acceleration_x": 0,
    "acceleration_y": 0.1,
    "braking_status": false,
    "fatigue_detected": false,
    "ignition_status": "ON",
    "fuel_level": 98.0
  }'

sleep 2

# 3. Trigger speeding violation
echo "3. Triggering speeding violation..."
curl -X POST http://localhost:8081/api/telemetry \
  -H "Content-Type: application/json" \
  -d '{
    "device_id": "DEV-E2E-001",
    "truck_id": "TRUCK-E2E-001",
    "driver_token": "DRV-E2E-001",
    "gps_lat": 34.0722,
    "gps_lon": -118.2437,
    "speed_instant": 85,
    "engine_rpm": 3500,
    "acceleration_x": 0,
    "acceleration_y": 0.2,
    "braking_status": false,
    "fatigue_detected": false,
    "ignition_status": "ON",
    "fuel_level": 96.0
  }'

sleep 2

# 4. Trigger harsh braking
echo "4. Triggering harsh braking..."
curl -X POST http://localhost:8081/api/telemetry \
  -H "Content-Type: application/json" \
  -d '{
    "device_id": "DEV-E2E-001",
    "truck_id": "TRUCK-E2E-001",
    "driver_token": "DRV-E2E-001",
    "gps_lat": 34.0822,
    "gps_lon": -118.2437,
    "speed_instant": 45,
    "engine_rpm": 1500,
    "acceleration_x": 0,
    "acceleration_y": -0.5,
    "braking_status": true,
    "fatigue_detected": false,
    "ignition_status": "ON",
    "fuel_level": 94.0
  }'

sleep 2

# 5. End trip (ignition OFF)
echo "5. Ending trip..."
curl -X POST http://localhost:8081/api/telemetry \
  -H "Content-Type: application/json" \
  -d '{
    "device_id": "DEV-E2E-001",
    "truck_id": "TRUCK-E2E-001",
    "driver_token": "DRV-E2E-001",
    "gps_lat": 34.0922,
    "gps_lon": -118.2437,
    "speed_instant": 0,
    "engine_rpm": 0,
    "acceleration_x": 0,
    "acceleration_y": 0,
    "braking_status": false,
    "fatigue_detected": false,
    "ignition_status": "OFF",
    "fuel_level": 92.0
  }'

sleep 3

# 6. Check driver score
echo "6. Checking driver score..."
curl http://localhost:8083/api/driver/DRV-E2E-001/score | jq

sleep 1

# 7. Check trips
echo "7. Checking trips..."
curl http://localhost:8083/api/driver/DRV-E2E-001/trips | jq

sleep 1

# 8. Check violations
echo "8. Checking violations..."
curl http://localhost:8083/api/driver/DRV-E2E-001/violations | jq

echo "=== E2E Test Complete ==="
```

### Expected Results

After running the E2E test:

1. **Driver Score**: Should be reduced (e.g., 75-80)
2. **Trip**: One completed trip with ~2.8 miles
3. **Violations**: Two violations (speeding + harsh braking)

---

## 4. Performance Testing

### Load Test with Apache JMeter

Create a JMeter test plan:
- Thread Group: 100 concurrent users
- HTTP Request: POST to /api/telemetry
- Duration: 5 minutes
- Ramp-up: 30 seconds

### Expected Throughput

| Service | Target TPS | Max Latency |
|---------|-----------|-------------|
| Telemetry Ingestion | 1000 | 100ms |
| Event Processing | 500 | 200ms |
| Driver Scoring API | 200 | 150ms |

---

## 5. Monitoring Tests

### Check Kafka Consumer Lag

```bash
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --group event-processing-group
```

Expected: Lag should be < 100 messages

### Check Database Performance

```sql
-- Check slow queries
SELECT * FROM pg_stat_statements 
ORDER BY mean_exec_time DESC 
LIMIT 10;

-- Check table sizes
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename))
FROM pg_tables 
WHERE schemaname = 'public';
```

---

## Common Test Scenarios

### Scenario 1: Normal Trip
- Start trip → Drive safely → End trip
- **Expected**: Score unchanged, trip recorded

### Scenario 2: Multiple Violations
- Start trip → Speed → Harsh brake → Harsh accelerate → End trip
- **Expected**: Score reduced by 25 points, 3 violations recorded

### Scenario 3: Long Trip
- Drive for 5+ hours without break
- **Expected**: Fatigue violation detected

### Scenario 4: Geofence Violation
- Drive outside allowed boundary
- **Expected**: Geofence violation recorded

---

## Debugging Tips

### View Kafka Messages

```bash
# Raw telemetry
kafka-console-consumer --topic raw_telemetry \
  --from-beginning --bootstrap-server localhost:9092

# Normalized telemetry
kafka-console-consumer --topic normalized_telemetry \
  --from-beginning --bootstrap-server localhost:9092

# Driving events
kafka-console-consumer --topic driving_events \
  --from-beginning --bootstrap-server localhost:9092
```

### Check Database State

```sql
-- Active trips
SELECT * FROM trips WHERE status = 'ACTIVE';

-- Recent violations
SELECT * FROM violations ORDER BY timestamp DESC LIMIT 10;

-- Driver scores
SELECT id, name, current_score FROM drivers ORDER BY current_score;
```

---

## CI/CD Testing Pipeline

Recommended GitHub Actions workflow:

```yaml
name: Test

on: [push, pull_request]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run unit tests
        run: mvn test
  
  integration-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run integration tests
        run: mvn verify -P integration-tests
```

---

## Test Coverage Goals

- **Unit Test Coverage**: > 80%
- **Integration Test Coverage**: > 60%
- **Critical Path Coverage**: 100%

Use JaCoCo for coverage reports:

```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```
