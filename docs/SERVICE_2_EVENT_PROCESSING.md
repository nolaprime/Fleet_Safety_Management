# Service 2: Event Processing Service

## 🎯 Service Overview

**Role**: The "Business Brain" - Cleans data and detects rule violations  
**Port**: 8082  
**Pattern**: Stream Processor (Consumer + Producer)  
**Consumes From**: `raw_telemetry` topic  
**Publishes To**: `normalized_telemetry` and `driving_events` topics

### Key Responsibilities
1. ✅ Consume raw telemetry from Kafka
2. ✅ Normalize and clean data (unit conversions, precision)
3. ✅ Apply sanity checks (discard impossible values)
4. ✅ Detect business rule violations (speeding, harsh braking, fatigue, geofence)
5. ✅ Publish to two separate streams (split-stream pattern)

### Learning Objectives
- Stream processing with Kafka consumers
- Split-stream pattern (one input → multiple outputs)
- Business rule implementation
- Data normalization techniques
- Stateless processing at scale

---

## 🏗️ Architecture

```
[raw_telemetry] Kafka Topic
       │
       │ Consumes
       ▼
┌─────────────────────────────────┐
│  TelemetryConsumer              │
│  - Polls messages in batches    │
│  - Deserializes JSON            │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│  NormalizationService           │
│  - Convert speed to MPH         │
│  - Normalize coordinates        │
│  - Discard impossible values    │
└────────┬────────────────────────┘
         │
         ├─────────────────────────┐
         │                         │
         ▼                         ▼
┌────────────────────┐  ┌──────────────────────────┐
│ RuleEvaluator      │  │ NormalizedDataPublisher  │
│ - Speed limit      │  │ - Publishes to           │
│ - Harsh braking    │  │   normalized_telemetry   │
│ - Fatigue          │  └──────────────────────────┘
│ - Geofence         │
└────────┬───────────┘
         │
         ▼
┌─────────────────────────────────┐
│  EventPublisher                 │
│  - Publishes violations to      │
│    driving_events topic          │
└─────────────────────────────────┘
```

---

## 📋 Data Specifications

### Input: Raw Telemetry (from Service 1)

```json
{
  "device_id": "DEV-12345",
  "truck_id": "TRUCK-789",
  "driver_token": "DRV-ABC-123",
  "gps_lat": 34.0522342,
  "gps_lon": -118.2437284,
  "speed_instant": 65.5,
  "engine_rpm": 2100,
  "acceleration_x": 0.05,
  "acceleration_y": -0.35,
  "braking_status": false,
  "fatigue_detected": false,
  "ignition_status": "ON",
  "fuel_level": 75.3,
  "timestamp": "2025-12-11T10:30:00Z"
}
```

### Output 1: Normalized Telemetry (Clean Data)

Published to: `normalized_telemetry`

```json
{
  "device_id": "DEV-12345",
  "truck_id": "TRUCK-789",
  "driver_id": "DRV-ABC-123",
  "gps_lat": 34.052200,
  "gps_lon": -118.243700,
  "speed_mph": 65.5,
  "engine_rpm": 2100,
  "ignition_status": "ON",
  "fuel_level": 75.3,
  "timestamp": "2025-12-11T10:30:00Z",
  "processed_at": "2025-12-11T10:30:00.500Z"
}
```

**Normalization Rules**:
- GPS coordinates: Round to 6 decimal places (~0.1m precision)
- Speed: Ensure in MPH (convert if needed)
- Remove acceleration data (not needed for trip calculation)
- Extract driver_id from driver_token
- Add `processed_at` timestamp

### Output 2: Driving Events (Violations)

Published to: `driving_events`

```json
{
  "event_id": "EVT-550e8400-e29b-41d4-a716-446655440000",
  "driver_id": "DRV-ABC-123",
  "truck_id": "TRUCK-789",
  "device_id": "DEV-12345",
  "event_type": "HARSH_BRAKING",
  "severity": "HIGH",
  "timestamp": "2025-12-11T10:30:00Z",
  "location": {
    "lat": 34.052200,
    "lon": -118.243700
  },
  "metadata": {
    "acceleration_y": -0.35,
    "speed_at_event": 65.5,
    "braking_distance_estimated": 45.2
  },
  "description": "Harsh braking detected: -0.35g deceleration at 65.5 MPH"
}
```

**Event Types**:
- `SPEEDING`
- `HARSH_BRAKING`
- `HARSH_ACCELERATION`
- `FATIGUE_DETECTED`
- `GEOFENCE_VIOLATION`

**Severity Levels**:
- `LOW`: Minor violation
- `MEDIUM`: Moderate risk
- `HIGH`: Serious safety concern
- `CRITICAL`: Immediate intervention required

---

## 🔧 Business Rules Implementation

### Rule 1: Speeding Detection

**Trigger**: `speed_instant > speed_limit`

```java
public Optional<DrivingEvent> detectSpeeding(TelemetryPayload telemetry) {
    double speedLimit = getSpeedLimit(telemetry.getGpsLat(), telemetry.getGpsLon());
    double speedMph = telemetry.getSpeedInstant();
    
    if (speedMph > speedLimit) {
        double excessSpeed = speedMph - speedLimit;
        
        Severity severity = determineSeverity(excessSpeed);
        
        return Optional.of(DrivingEvent.builder()
            .eventType(EventType.SPEEDING)
            .severity(severity)
            .metadata(Map.of(
                "speed_mph", speedMph,
                "speed_limit", speedLimit,
                "excess_speed", excessSpeed
            ))
            .description(String.format("Speeding: %.1f MPH in %.0f MPH zone", 
                speedMph, speedLimit))
            .build());
    }
    
    return Optional.empty();
}

private Severity determineSeverity(double excessSpeed) {
    if (excessSpeed > 25) return Severity.CRITICAL;
    if (excessSpeed > 15) return Severity.HIGH;
    if (excessSpeed > 10) return Severity.MEDIUM;
    return Severity.LOW;
}
```

**Mock Speed Limit Logic**:
```java
private double getSpeedLimit(double lat, double lon) {
    // For teaching: Simple mock logic
    // In production: Use external API or geospatial database
    
    // Highway detection (simplified)
    if (isHighway(lat, lon)) {
        return 70.0; // Highway speed limit
    }
    
    // Urban area
    if (isUrbanArea(lat, lon)) {
        return 35.0;
    }
    
    // Default: suburban
    return 55.0;
}
```

### Rule 2: Harsh Braking Detection

**Trigger**: `acceleration_y < -0.3g` (sudden deceleration)

```java
public Optional<DrivingEvent> detectHarshBraking(TelemetryPayload telemetry) {
    double accelerationY = telemetry.getAccelerationY();
    
    // Negative acceleration = braking
    if (accelerationY < -0.3) {
        double brakingForce = Math.abs(accelerationY);
        
        // Calculate estimated stopping distance
        double speedMph = telemetry.getSpeedInstant();
        double stoppingDistance = calculateStoppingDistance(speedMph, brakingForce);
        
        Severity severity = determineBrakingSeverity(brakingForce);
        
        return Optional.of(DrivingEvent.builder()
            .eventType(EventType.HARSH_BRAKING)
            .severity(severity)
            .metadata(Map.of(
                "acceleration_y", accelerationY,
                "speed_at_event", speedMph,
                "braking_distance_estimated", stoppingDistance
            ))
            .description(String.format("Harsh braking: %.2fg deceleration at %.1f MPH", 
                brakingForce, speedMph))
            .build());
    }
    
    return Optional.empty();
}

private Severity determineBrakingSeverity(double gForce) {
    if (gForce > 0.8) return Severity.CRITICAL; // Emergency braking
    if (gForce > 0.5) return Severity.HIGH;
    if (gForce > 0.4) return Severity.MEDIUM;
    return Severity.LOW;
}
```

### Rule 3: Fatigue Detection

**Trigger**: `fatigue_detected == true` OR `continuous_driving_time > 4 hours`

```java
public Optional<DrivingEvent> detectFatigue(TelemetryPayload telemetry, 
                                            DriverState driverState) {
    
    // Direct sensor detection
    if (telemetry.getFatigueDetected()) {
        return Optional.of(DrivingEvent.builder()
            .eventType(EventType.FATIGUE_DETECTED)
            .severity(Severity.HIGH)
            .metadata(Map.of(
                "detection_method", "sensor",
                "continuous_driving_hours", driverState.getContinuousDrivingHours()
            ))
            .description("Driver fatigue detected by onboard sensors")
            .build());
    }
    
    // Time-based detection
    if (driverState.getContinuousDrivingHours() > 4.0) {
        return Optional.of(DrivingEvent.builder()
            .eventType(EventType.FATIGUE_DETECTED)
            .severity(Severity.MEDIUM)
            .metadata(Map.of(
                "detection_method", "time_based",
                "continuous_driving_hours", driverState.getContinuousDrivingHours()
            ))
            .description(String.format("Extended driving: %.1f hours without break", 
                driverState.getContinuousDrivingHours()))
            .build());
    }
    
    return Optional.empty();
}
```

**Driver State Tracking** (In-Memory):
```java
@Component
public class DriverStateManager {
    
    private final Map<String, DriverState> driverStates = new ConcurrentHashMap<>();
    
    public void updateState(TelemetryPayload telemetry) {
        String driverId = extractDriverId(telemetry.getDriverToken());
        
        driverStates.compute(driverId, (key, state) -> {
            if (state == null) {
                state = new DriverState(driverId);
            }
            
            if ("ON".equals(telemetry.getIgnitionStatus())) {
                state.addDrivingTime(telemetry);
            } else {
                state.resetDrivingTime();
            }
            
            return state;
        });
    }
    
    public DriverState getState(String driverId) {
        return driverStates.getOrDefault(driverId, new DriverState(driverId));
    }
}
```

### Rule 4: Geofence Violation

**Trigger**: GPS coordinates outside allowed boundary

```java
public Optional<DrivingEvent> detectGeofenceViolation(TelemetryPayload telemetry) {
    double lat = telemetry.getGpsLat();
    double lon = telemetry.getGpsLon();
    
    // For teaching: Simple bounding box
    // Production: Use PostGIS or geospatial libraries
    if (!isWithinGeofence(lat, lon)) {
        
        double distanceFromBoundary = calculateDistanceFromBoundary(lat, lon);
        
        return Optional.of(DrivingEvent.builder()
            .eventType(EventType.GEOFENCE_VIOLATION)
            .severity(Severity.HIGH)
            .metadata(Map.of(
                "distance_from_boundary_km", distanceFromBoundary,
                "boundary_type", "operational_zone"
            ))
            .description(String.format("Vehicle outside operational zone by %.2f km", 
                distanceFromBoundary))
            .build());
    }
    
    return Optional.empty();
}

private boolean isWithinGeofence(double lat, double lon) {
    // Simple bounding box for Los Angeles area (example)
    double minLat = 33.7;
    double maxLat = 34.3;
    double minLon = -118.7;
    double maxLon = -118.0;
    
    return lat >= minLat && lat <= maxLat && 
           lon >= minLon && lon <= maxLon;
}
```

### Rule 5: Harsh Acceleration

**Trigger**: `acceleration_y > 0.4g` (rapid acceleration)

```java
public Optional<DrivingEvent> detectHarshAcceleration(TelemetryPayload telemetry) {
    double accelerationY = telemetry.getAccelerationY();
    
    if (accelerationY > 0.4) {
        Severity severity = accelerationY > 0.7 ? Severity.HIGH : Severity.MEDIUM;
        
        return Optional.of(DrivingEvent.builder()
            .eventType(EventType.HARSH_ACCELERATION)
            .severity(severity)
            .metadata(Map.of(
                "acceleration_y", accelerationY,
                "speed_at_event", telemetry.getSpeedInstant()
            ))
            .description(String.format("Harsh acceleration: %.2fg", accelerationY))
            .build());
    }
    
    return Optional.empty();
}
```

---

## 🔧 Implementation Guide

### Project Structure

```
event-processing-service/
├── src/
│   ├── main/
│   │   ├── java/com/fleet/processor/
│   │   │   ├── EventProcessingApplication.java
│   │   │   ├── consumer/
│   │   │   │   └── TelemetryConsumer.java
│   │   │   ├── service/
│   │   │   │   ├── NormalizationService.java
│   │   │   │   ├── RuleEvaluationService.java
│   │   │   │   ├── DriverStateManager.java
│   │   │   │   └── EventPublisher.java
│   │   │   ├── rules/
│   │   │   │   ├── SpeedingRule.java
│   │   │   │   ├── HarshBrakingRule.java
│   │   │   │   ├── FatigueRule.java
│   │   │   │   ├── GeofenceRule.java
│   │   │   │   └── RuleEngine.java
│   │   │   ├── model/
│   │   │   │   ├── TelemetryPayload.java
│   │   │   │   ├── NormalizedTelemetry.java
│   │   │   │   ├── DrivingEvent.java
│   │   │   │   ├── DriverState.java
│   │   │   │   ├── EventType.java
│   │   │   │   └── Severity.java
│   │   │   ├── config/
│   │   │   │   └── KafkaConsumerConfig.java
│   │   │   └── util/
│   │   │       ├── GeoUtil.java
│   │   │       └── PhysicsUtil.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── geofence-boundaries.json
│   └── test/
│       └── java/com/fleet/processor/
│           ├── rules/
│           │   └── RuleEngineTest.java
│           └── service/
│               └── NormalizationServiceTest.java
├── pom.xml
└── README.md
```

### Key Classes

#### 1. TelemetryConsumer.java

```java
package com.fleet.processor.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet.processor.model.TelemetryPayload;
import com.fleet.processor.service.NormalizationService;
import com.fleet.processor.service.RuleEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelemetryConsumer {
    
    private final ObjectMapper objectMapper;
    private final NormalizationService normalizationService;
    private final RuleEvaluationService ruleEvaluationService;
    
    @KafkaListener(
        topics = "raw_telemetry",
        groupId = "event-processing-group",
        concurrency = "3"
    )
    public void consumeTelemetry(String message) {
        try {
            log.debug("Received raw telemetry: {}", message);
            
            // Deserialize
            TelemetryPayload payload = objectMapper.readValue(
                message, TelemetryPayload.class);
            
            // Normalize data
            var normalized = normalizationService.normalize(payload);
            
            // Evaluate rules and generate events
            ruleEvaluationService.evaluateAndPublish(payload, normalized);
            
        } catch (Exception e) {
            log.error("Error processing telemetry: {}", e.getMessage(), e);
            // In production: send to dead letter queue
        }
    }
}
```

#### 2. NormalizationService.java

```java
package com.fleet.processor.service;

import com.fleet.processor.model.NormalizedTelemetry;
import com.fleet.processor.model.TelemetryPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class NormalizationService {
    
    private static final String NORMALIZED_TOPIC = "normalized_telemetry";
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    public NormalizedTelemetry normalize(TelemetryPayload raw) {
        // Sanity checks
        if (!isSane(raw)) {
            log.warn("Discarding insane telemetry: speed={}, rpm={}", 
                raw.getSpeedInstant(), raw.getEngineRpm());
            return null;
        }
        
        // Build normalized payload
        NormalizedTelemetry normalized = NormalizedTelemetry.builder()
            .deviceId(raw.getDeviceId())
            .truckId(raw.getTruckId())
            .driverId(extractDriverId(raw.getDriverToken()))
            .gpsLat(roundCoordinate(raw.getGpsLat()))
            .gpsLon(roundCoordinate(raw.getGpsLon()))
            .speedMph(raw.getSpeedInstant())
            .engineRpm(raw.getEngineRpm())
            .ignitionStatus(raw.getIgnitionStatus())
            .fuelLevel(raw.getFuelLevel())
            .timestamp(raw.getTimestamp())
            .processedAt(Instant.now())
            .build();
        
        // Publish to normalized_telemetry topic
        publishNormalized(normalized);
        
        return normalized;
    }
    
    private boolean isSane(TelemetryPayload raw) {
        // Impossible speed
        if (raw.getSpeedInstant() > 150) return false;
        
        // Impossible RPM
        if (raw.getEngineRpm() > 8000) return false;
        
        // Invalid coordinates
        if (Math.abs(raw.getGpsLat()) > 90) return false;
        if (Math.abs(raw.getGpsLon()) > 180) return false;
        
        return true;
    }
    
    private double roundCoordinate(double coord) {
        return BigDecimal.valueOf(coord)
            .setScale(6, RoundingMode.HALF_UP)
            .doubleValue();
    }
    
    private String extractDriverId(String driverToken) {
        // DRV-ABC-123 -> DRV-ABC-123 (simplified)
        return driverToken;
    }
    
    private void publishNormalized(NormalizedTelemetry telemetry) {
        // Implementation similar to producer service
        // ...
    }
}
```

#### 3. RuleEngine.java

```java
package com.fleet.processor.rules;

import com.fleet.processor.model.DrivingEvent;
import com.fleet.processor.model.TelemetryPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RuleEngine {
    
    private final List<Rule> rules;
    
    public List<DrivingEvent> evaluate(TelemetryPayload telemetry) {
        List<DrivingEvent> events = new ArrayList<>();
        
        for (Rule rule : rules) {
            rule.evaluate(telemetry).ifPresent(events::add);
        }
        
        return events;
    }
}

// Base interface
public interface Rule {
    Optional<DrivingEvent> evaluate(TelemetryPayload telemetry);
}
```

### Configuration

#### application.yml

```yaml
spring:
  application:
    name: event-processing-service
  
  kafka:
    bootstrap-servers: localhost:9092
    
    consumer:
      group-id: event-processing-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      enable-auto-commit: true
      max-poll-records: 500
      
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: 1
      
server:
  port: 8082

logging:
  level:
    com.fleet.processor: DEBUG
    org.apache.kafka: INFO

# Business rule configuration
rules:
  speed-limit:
    highway: 70
    urban: 35
    suburban: 55
  harsh-braking-threshold: -0.3
  harsh-acceleration-threshold: 0.4
  fatigue-hours-threshold: 4.0
  geofence:
    enabled: true
    boundaries:
      min-lat: 33.7
      max-lat: 34.3
      min-lon: -118.7
      max-lon: -118.0
```

---

## 🧪 Testing

### Unit Test: Rule Evaluation

```java
@Test
void shouldDetectHarshBraking() {
    TelemetryPayload telemetry = TelemetryPayload.builder()
        .accelerationY(-0.4)
        .speedInstant(60.0)
        .build();
    
    Optional<DrivingEvent> event = harshBrakingRule.evaluate(telemetry);
    
    assertTrue(event.isPresent());
    assertEquals(EventType.HARSH_BRAKING, event.get().getEventType());
    assertEquals(Severity.MEDIUM, event.get().getSeverity());
}
```

### Integration Test: End-to-End Flow

```java
@SpringBootTest
@EmbeddedKafka(topics = {"raw_telemetry", "normalized_telemetry", "driving_events"})
class EventProcessingIntegrationTest {
    
    @Test
    void shouldProcessTelemetryAndGenerateEvent() throws Exception {
        // Publish to raw_telemetry
        // Verify message in normalized_telemetry
        // Verify event in driving_events
    }
}
```

---

## 📚 Teaching Discussion Points

### 1. Split-Stream Pattern
- One input produces multiple outputs
- Different consumers need different data shapes
- Enables parallel processing of clean data vs events

### 2. Stateless vs Stateful Processing
- Most rules are stateless (current message only)
- Fatigue detection requires state (continuous driving time)
- Trade-off: simplicity vs capability

### 3. Consumer Configuration
- `max-poll-records`: Batch size for efficiency
- `auto-offset-reset`: Where to start reading
- `enable-auto-commit`: Automatic vs manual offset management

### 4. Error Handling
- Discard invalid data vs retry
- Dead letter queues for failed processing
- Monitoring consumer lag

---

## 🚀 Next Steps

1. ✅ Service consumes raw telemetry
2. ✅ Data normalized and cleaned
3. ✅ Business rules evaluated
4. ✅ Two output streams published
5. → Build Service 3: Driver Scoring & Trip History
6. → See [SERVICE_3_DRIVER_SCORING.md](SERVICE_3_DRIVER_SCORING.md)
