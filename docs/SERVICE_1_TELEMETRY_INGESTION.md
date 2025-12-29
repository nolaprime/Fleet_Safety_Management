# Service 1: Telemetry Ingestion Service

## 🎯 Service Overview

**Role**: High-throughput entry point for IoT device telemetry data  
**Port**: 8081  
**Pattern**: Producer (Fire-and-Forget)  
**Publishes To**: `raw_telemetry` Kafka topic

### Key Responsibilities
1. ✅ Validate incoming telemetry payload structure
2. ✅ Authenticate devices/drivers via tokens
3. ✅ Perform basic sanity checks
4. ✅ Publish to Kafka immediately (fire-and-forget)
5. ✅ Handle errors gracefully without blocking

### Learning Objectives
- Understanding Kafka producers
- Asynchronous messaging patterns
- REST API design for high throughput
- Input validation and sanitization
- Error handling in distributed systems

---

## 🏗️ Architecture

```
┌─────────────┐
│ IoT Device  │
│  (Truck)    │
└──────┬──────┘
       │ HTTP POST
       │ /api/telemetry
       ▼
┌──────────────────────────────┐
│  TelemetryController         │
│  - Validates JSON structure  │
│  - Checks required fields    │
└──────────┬───────────────────┘
           │
           ▼
┌──────────────────────────────┐
│  AuthenticationService       │
│  - Validates driver_token    │
│  - Validates device_id       │
└──────────┬───────────────────┘
           │
           ▼
┌──────────────────────────────┐
│  TelemetryService            │
│  - Enriches with timestamp   │
│  - Basic sanity checks       │
└──────────┬───────────────────┘
           │
           ▼
┌──────────────────────────────┐
│  KafkaProducerService        │
│  - Serializes to JSON        │
│  - Sends to raw_telemetry    │
└──────────┬───────────────────┘
           │
           ▼
    [raw_telemetry] Kafka Topic
```

---

## 📋 API Specification

### Endpoint: Ingest Telemetry

**URL**: `POST /api/telemetry`  
**Content-Type**: `application/json`  
**Authentication**: Bearer token (optional for teaching, required for production)

#### Request Payload

```json
{
  "device_id": "DEV-12345",
  "truck_id": "TRUCK-789",
  "driver_token": "DRV-ABC-123",
  "gps_lat": 34.0522,
  "gps_lon": -118.2437,
  "speed_instant": 65.5,
  "engine_rpm": 2100,
  "acceleration_x": 0.05,
  "acceleration_y": -0.15,
  "braking_status": false,
  "fatigue_detected": false,
  "ignition_status": "ON",
  "fuel_level": 75.3,
  "timestamp": "2025-12-11T10:30:00Z"
}
```

#### Field Specifications

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `device_id` | String | Yes | Pattern: `DEV-\d+` | Unique IoT device identifier |
| `truck_id` | String | Yes | Pattern: `TRUCK-\d+` | Fleet vehicle identifier |
| `driver_token` | String | Yes | Pattern: `DRV-[A-Z]+-\d+` | Driver authentication token |
| `gps_lat` | Double | Yes | Range: -90 to 90 | Latitude in decimal degrees |
| `gps_lon` | Double | Yes | Range: -180 to 180 | Longitude in decimal degrees |
| `speed_instant` | Double | Yes | Range: 0 to 200 | Speed in MPH |
| `engine_rpm` | Integer | Yes | Range: 0 to 8000 | Engine RPM |
| `acceleration_x` | Double | Yes | Range: -2.0 to 2.0 | Lateral G-force |
| `acceleration_y` | Double | Yes | Range: -2.0 to 2.0 | Longitudinal G-force |
| `braking_status` | Boolean | Yes | true/false | Emergency braking active |
| `fatigue_detected` | Boolean | Yes | true/false | Driver fatigue sensor alert |
| `ignition_status` | String | Yes | Enum: ON, OFF | Engine ignition state |
| `fuel_level` | Double | Yes | Range: 0 to 100 | Fuel percentage |
| `timestamp` | String | No | ISO-8601 format | Event timestamp (server adds if missing) |

#### Success Response

**Status**: `202 Accepted`

```json
{
  "status": "accepted",
  "message": "Telemetry data accepted for processing",
  "telemetry_id": "TEL-UUID-12345",
  "received_at": "2025-12-11T10:30:01.234Z"
}
```

#### Error Responses

**Status**: `400 Bad Request` - Invalid payload

```json
{
  "status": "error",
  "message": "Validation failed",
  "errors": [
    {
      "field": "gps_lat",
      "error": "must be between -90 and 90"
    },
    {
      "field": "driver_token",
      "error": "invalid format"
    }
  ]
}
```

**Status**: `401 Unauthorized` - Authentication failed

```json
{
  "status": "error",
  "message": "Invalid driver token or device ID"
}
```

**Status**: `503 Service Unavailable` - Kafka unavailable

```json
{
  "status": "error",
  "message": "Message broker temporarily unavailable",
  "retry_after": 30
}
```

---

## 🔧 Implementation Guide

### Project Structure

```
telemetry-ingestion-service/
├── src/
│   ├── main/
│   │   ├── java/com/fleet/telemetry/
│   │   │   ├── TelemetryIngestionApplication.java
│   │   │   ├── controller/
│   │   │   │   └── TelemetryController.java
│   │   │   ├── service/
│   │   │   │   ├── TelemetryService.java
│   │   │   │   ├── AuthenticationService.java
│   │   │   │   └── KafkaProducerService.java
│   │   │   ├── model/
│   │   │   │   ├── TelemetryPayload.java
│   │   │   │   └── TelemetryResponse.java
│   │   │   ├── validation/
│   │   │   │   └── TelemetryValidator.java
│   │   │   ├── config/
│   │   │   │   └── KafkaProducerConfig.java
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       └── InvalidTelemetryException.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   └── test/
│       └── java/com/fleet/telemetry/
│           ├── controller/
│           │   └── TelemetryControllerTest.java
│           └── service/
│               └── KafkaProducerServiceTest.java
├── pom.xml
└── README.md
```

### Key Classes

#### 1. TelemetryPayload.java

```java
package com.fleet.telemetry.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.Instant;

@Data
public class TelemetryPayload {
    
    @NotBlank(message = "device_id is required")
    @Pattern(regexp = "DEV-\\d+", message = "device_id must match pattern DEV-{number}")
    private String deviceId;
    
    @NotBlank(message = "truck_id is required")
    @Pattern(regexp = "TRUCK-\\d+", message = "truck_id must match pattern TRUCK-{number}")
    private String truckId;
    
    @NotBlank(message = "driver_token is required")
    @Pattern(regexp = "DRV-[A-Z]+-\\d+", message = "driver_token must match pattern DRV-{LETTERS}-{number}")
    private String driverToken;
    
    @NotNull(message = "gps_lat is required")
    @DecimalMin(value = "-90.0", message = "gps_lat must be >= -90")
    @DecimalMax(value = "90.0", message = "gps_lat must be <= 90")
    private Double gpsLat;
    
    @NotNull(message = "gps_lon is required")
    @DecimalMin(value = "-180.0", message = "gps_lon must be >= -180")
    @DecimalMax(value = "180.0", message = "gps_lon must be <= 180")
    private Double gpsLon;
    
    @NotNull(message = "speed_instant is required")
    @DecimalMin(value = "0.0", message = "speed_instant must be >= 0")
    @DecimalMax(value = "200.0", message = "speed_instant must be <= 200")
    private Double speedInstant;
    
    @NotNull(message = "engine_rpm is required")
    @Min(value = 0, message = "engine_rpm must be >= 0")
    @Max(value = 8000, message = "engine_rpm must be <= 8000")
    private Integer engineRpm;
    
    @NotNull(message = "acceleration_x is required")
    @DecimalMin(value = "-2.0", message = "acceleration_x must be >= -2.0")
    @DecimalMax(value = "2.0", message = "acceleration_x must be <= 2.0")
    private Double accelerationX;
    
    @NotNull(message = "acceleration_y is required")
    @DecimalMin(value = "-2.0", message = "acceleration_y must be >= -2.0")
    @DecimalMax(value = "2.0", message = "acceleration_y must be <= 2.0")
    private Double accelerationY;
    
    @NotNull(message = "braking_status is required")
    private Boolean brakingStatus;
    
    @NotNull(message = "fatigue_detected is required")
    private Boolean fatigueDetected;
    
    @NotBlank(message = "ignition_status is required")
    @Pattern(regexp = "ON|OFF", message = "ignition_status must be ON or OFF")
    private String ignitionStatus;
    
    @NotNull(message = "fuel_level is required")
    @DecimalMin(value = "0.0", message = "fuel_level must be >= 0")
    @DecimalMax(value = "100.0", message = "fuel_level must be <= 100")
    private Double fuelLevel;
    
    // Optional - will be set by server if not provided
    private Instant timestamp;
}
```

#### 2. TelemetryController.java

```java
package com.fleet.telemetry.controller;

import com.fleet.telemetry.model.TelemetryPayload;
import com.fleet.telemetry.model.TelemetryResponse;
import com.fleet.telemetry.service.TelemetryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/telemetry")
@RequiredArgsConstructor
public class TelemetryController {
    
    private final TelemetryService telemetryService;
    
    @PostMapping
    public ResponseEntity<TelemetryResponse> ingestTelemetry(
            @Valid @RequestBody TelemetryPayload payload) {
        
        log.info("Received telemetry from device: {}", payload.getDeviceId());
        
        TelemetryResponse response = telemetryService.processTelemetry(payload);
        
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Telemetry Ingestion Service is running");
    }
}
```

#### 3. KafkaProducerService.java

```java
package com.fleet.telemetry.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet.telemetry.model.TelemetryPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    
    private static final String TOPIC = "raw_telemetry";
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    public void sendTelemetry(TelemetryPayload payload) {
        try {
            // Use device_id as the key for partitioning
            String key = payload.getDeviceId();
            String value = objectMapper.writeValueAsString(payload);
            
            // Fire-and-forget pattern with async callback
            CompletableFuture<SendResult<String, String>> future = 
                    kafkaTemplate.send(TOPIC, key, value);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Sent telemetry to partition {} with offset {}",
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send telemetry for device {}: {}",
                            key, ex.getMessage());
                }
            });
            
        } catch (Exception e) {
            log.error("Error serializing telemetry payload", e);
            throw new RuntimeException("Failed to publish telemetry", e);
        }
    }
}
```

### Configuration

#### application.yml

```yaml
spring:
  application:
    name: telemetry-ingestion-service
  
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: 1  # Leader acknowledgment (balance between speed and reliability)
      retries: 3
      batch-size: 16384
      linger-ms: 10  # Small delay to batch messages
      compression-type: lz4  # Fast compression
      
server:
  port: 8081

logging:
  level:
    com.fleet.telemetry: DEBUG
    org.apache.kafka: INFO
```

### pom.xml Dependencies

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.fleet</groupId>
    <artifactId>telemetry-ingestion-service</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring Kafka -->
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
        
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 🧪 Testing

### Unit Tests

```java
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"raw_telemetry"})
class TelemetryControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldAcceptValidTelemetry() throws Exception {
        String payload = """
            {
              "device_id": "DEV-12345",
              "truck_id": "TRUCK-789",
              "driver_token": "DRV-ABC-123",
              "gps_lat": 34.0522,
              "gps_lon": -118.2437,
              "speed_instant": 65.5,
              "engine_rpm": 2100,
              "acceleration_x": 0.05,
              "acceleration_y": -0.15,
              "braking_status": false,
              "fatigue_detected": false,
              "ignition_status": "ON",
              "fuel_level": 75.3
            }
            """;
        
        mockMvc.perform(post("/api/telemetry")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("accepted"));
    }
    
    @Test
    void shouldRejectInvalidLatitude() throws Exception {
        String payload = """
            {
              "device_id": "DEV-12345",
              "truck_id": "TRUCK-789",
              "driver_token": "DRV-ABC-123",
              "gps_lat": 95.0,
              "gps_lon": -118.2437,
              ...
            }
            """;
        
        mockMvc.perform(post("/api/telemetry")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }
}
```

### Manual Testing with curl

```bash
# Valid telemetry
curl -X POST http://localhost:8081/api/telemetry \
  -H "Content-Type: application/json" \
  -d '{
    "device_id": "DEV-12345",
    "truck_id": "TRUCK-789",
    "driver_token": "DRV-ABC-123",
    "gps_lat": 34.0522,
    "gps_lon": -118.2437,
    "speed_instant": 65.5,
    "engine_rpm": 2100,
    "acceleration_x": 0.05,
    "acceleration_y": -0.15,
    "braking_status": false,
    "fatigue_detected": false,
    "ignition_status": "ON",
    "fuel_level": 75.3
  }'

# Verify in Kafka
kafka-console-consumer \
  --topic raw_telemetry \
  --from-beginning \
  --bootstrap-server localhost:9092
```

---

## 📚 Teaching Discussion Points

### 1. Why Fire-and-Forget?
- **Performance**: Don't wait for Kafka acknowledgment
- **Scalability**: Handle thousands of requests/second
- **Trade-off**: Risk of message loss vs throughput

### 2. Producer Configuration
- **acks=1**: Balance between speed and reliability
- **retries=3**: Automatic retry on transient failures
- **compression**: Reduce network bandwidth

### 3. Partitioning Strategy
- Using `device_id` as key ensures messages from same device go to same partition
- Maintains ordering per device
- Enables parallel processing

### 4. Error Handling
- Validate early, fail fast
- Return 202 (Accepted) immediately
- Handle Kafka errors asynchronously

---

## 🚀 Next Steps

After completing this service:
1. ✅ Service accepts and validates telemetry
2. ✅ Messages published to Kafka
3. → Build Service 2: Event Processing Service
4. → See [SERVICE_2_EVENT_PROCESSING.md](SERVICE_2_EVENT_PROCESSING.md)

---

## 📝 Student Exercise Checklist

- [ ] Understand REST API design for high throughput
- [ ] Implement validation using Jakarta Validation
- [ ] Configure Kafka producer
- [ ] Implement fire-and-forget pattern
- [ ] Handle errors gracefully
- [ ] Write unit tests with embedded Kafka
- [ ] Test with curl and Kafka console consumer
- [ ] Monitor Kafka topic in Kafka UI
