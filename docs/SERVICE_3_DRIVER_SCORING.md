# Service 3: Driver Scoring & Trip History Service

## 🎯 Service Overview

**Role**: The "System of Record" - Stateful aggregator maintaining driver state  
**Port**: 8083  
**Pattern**: Stateful Consumer + REST API  
**Consumes From**: `normalized_telemetry` and `driving_events` topics  
**Database**: PostgreSQL  
**Exposes**: REST API for driver scores, trips, and violations

### Key Responsibilities
1. ✅ Consume normalized telemetry to calculate trip distance
2. ✅ Consume driving events to update driver scores
3. ✅ Maintain trip state machine (start/end trips based on ignition)
4. ✅ Persist trips, violations, and scores to database
5. ✅ Expose REST API for querying driver data
6. ✅ Implement event sourcing pattern

### Learning Objectives
- Stateful stream processing
- Event sourcing and state reconstruction
- State machines (trip lifecycle)
- Database integration with event-driven architecture
- REST API design for query endpoints
- Haversine formula for GPS distance calculation

---

## 🏗️ Architecture

```
[normalized_telemetry] Topic    [driving_events] Topic
        │                              │
        │ Consumes                     │ Consumes
        ▼                              ▼
┌────────────────────────────────────────────────┐
│  TelemetryConsumer      EventConsumer          │
└─────────┬─────────────────────┬────────────────┘
          │                     │
          ▼                     ▼
┌─────────────────────┐  ┌──────────────────────┐
│  TripStateMachine   │  │  ScoringEngine       │
│  - Track ignition   │  │  - Calculate points  │
│  - Start/End trips  │  │  - Link to trips     │
│  - Calculate miles  │  │  - Update scores     │
└─────────┬───────────┘  └──────────┬───────────┘
          │                         │
          └──────────┬──────────────┘
                     │
                     ▼
          ┌──────────────────────┐
          │  PostgreSQL Database │
          │  - drivers           │
          │  - trips             │
          │  - violations        │
          └──────────────────────┘
                     │
                     ▼
          ┌──────────────────────┐
          │   REST API Layer     │
          │  - GET /score        │
          │  - GET /trips        │
          │  - GET /violations   │
          └──────────────────────┘
```

---

## 📊 Database Schema

### Table: drivers

```sql
CREATE TABLE drivers (
    id VARCHAR(50) PRIMARY KEY,                    -- DRV-ABC-123
    name VARCHAR(100),
    current_score INTEGER DEFAULT 100,              -- Starts at 100
    total_trips INTEGER DEFAULT 0,
    total_miles DECIMAL(10, 2) DEFAULT 0.0,
    total_violations INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT score_range CHECK (current_score >= 0 AND current_score <= 100)
);

CREATE INDEX idx_drivers_score ON drivers(current_score);
```

### Table: trips

```sql
CREATE TABLE trips (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id VARCHAR(50) NOT NULL REFERENCES drivers(id),
    truck_id VARCHAR(50) NOT NULL,
    device_id VARCHAR(50) NOT NULL,
    
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,                             -- NULL if trip active
    duration_minutes INTEGER,                       -- Calculated on end
    
    start_location_lat DECIMAL(9, 6),
    start_location_lon DECIMAL(9, 6),
    end_location_lat DECIMAL(9, 6),
    end_location_lon DECIMAL(9, 6),
    
    total_miles DECIMAL(10, 2) DEFAULT 0.0,
    violation_count INTEGER DEFAULT 0,
    
    status VARCHAR(20) DEFAULT 'ACTIVE',            -- ACTIVE, COMPLETED
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_trips_driver ON trips(driver_id);
CREATE INDEX idx_trips_status ON trips(status);
CREATE INDEX idx_trips_start_time ON trips(start_time DESC);
```

### Table: violations

```sql
CREATE TABLE violations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID REFERENCES trips(id),
    driver_id VARCHAR(50) NOT NULL REFERENCES drivers(id),
    truck_id VARCHAR(50) NOT NULL,
    
    event_type VARCHAR(50) NOT NULL,                -- SPEEDING, HARSH_BRAKING, etc.
    severity VARCHAR(20) NOT NULL,                  -- LOW, MEDIUM, HIGH, CRITICAL
    
    location_lat DECIMAL(9, 6) NOT NULL,
    location_lon DECIMAL(9, 6) NOT NULL,
    
    points_deducted INTEGER NOT NULL,               -- Penalty applied
    
    metadata JSONB,                                 -- Additional event details
    description TEXT,
    
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_violations_driver ON violations(driver_id);
CREATE INDEX idx_violations_trip ON violations(trip_id);
CREATE INDEX idx_violations_timestamp ON violations(timestamp DESC);
CREATE INDEX idx_violations_type ON violations(event_type);
```

### Sample Data

```sql
-- Sample driver
INSERT INTO drivers (id, name, current_score, total_trips, total_miles, total_violations)
VALUES ('DRV-ABC-123', 'John Doe', 85, 45, 2340.50, 12);

-- Sample trip
INSERT INTO trips (id, driver_id, truck_id, device_id, start_time, end_time, 
                   start_location_lat, start_location_lon, 
                   end_location_lat, end_location_lon, 
                   total_miles, violation_count, status)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 
        'DRV-ABC-123', 'TRUCK-789', 'DEV-12345',
        '2025-12-11 08:00:00', '2025-12-11 10:30:00',
        34.0522, -118.2437, 34.1522, -118.1437,
        52.3, 2, 'COMPLETED');

-- Sample violation
INSERT INTO violations (trip_id, driver_id, truck_id, event_type, severity,
                       location_lat, location_lon, points_deducted, 
                       description, timestamp)
VALUES ('550e8400-e29b-41d4-a716-446655440000',
        'DRV-ABC-123', 'TRUCK-789', 'HARSH_BRAKING', 'HIGH',
        34.0722, -118.2337, 10,
        'Harsh braking: -0.45g deceleration at 65.5 MPH',
        '2025-12-11 09:15:00');
```

---

## 🔧 Trip State Machine

### States
1. **NO_TRIP**: Driver not currently driving
2. **TRIP_ACTIVE**: Ignition ON, trip in progress
3. **TRIP_COMPLETED**: Ignition OFF, trip ended

### Transitions

```
NO_TRIP --[ignition ON]--> TRIP_ACTIVE
TRIP_ACTIVE --[ignition OFF]--> TRIP_COMPLETED --[persist]--> NO_TRIP
```

### Implementation

```java
@Component
@RequiredArgsConstructor
public class TripStateMachine {
    
    private final Map<String, ActiveTrip> activeTripsByDriver = new ConcurrentHashMap<>();
    private final TripRepository tripRepository;
    
    public void processNormalizedTelemetry(NormalizedTelemetry telemetry) {
        String driverId = telemetry.getDriverId();
        String ignitionStatus = telemetry.getIgnitionStatus();
        
        if ("ON".equals(ignitionStatus)) {
            handleIgnitionOn(driverId, telemetry);
        } else if ("OFF".equals(ignitionStatus)) {
            handleIgnitionOff(driverId, telemetry);
        }
    }
    
    private void handleIgnitionOn(String driverId, NormalizedTelemetry telemetry) {
        activeTripsByDriver.compute(driverId, (key, activeTrip) -> {
            if (activeTrip == null) {
                // Start new trip
                log.info("Starting trip for driver: {}", driverId);
                return startNewTrip(telemetry);
            } else {
                // Update existing trip (calculate distance)
                return updateTrip(activeTrip, telemetry);
            }
        });
    }
    
    private void handleIgnitionOff(String driverId, NormalizedTelemetry telemetry) {
        ActiveTrip activeTrip = activeTripsByDriver.remove(driverId);
        
        if (activeTrip != null) {
            log.info("Ending trip for driver: {}", driverId);
            endTrip(activeTrip, telemetry);
        }
    }
    
    private ActiveTrip startNewTrip(NormalizedTelemetry telemetry) {
        return ActiveTrip.builder()
            .tripId(UUID.randomUUID())
            .driverId(telemetry.getDriverId())
            .truckId(telemetry.getTruckId())
            .deviceId(telemetry.getDeviceId())
            .startTime(telemetry.getTimestamp())
            .startLat(telemetry.getGpsLat())
            .startLon(telemetry.getGpsLon())
            .lastLat(telemetry.getGpsLat())
            .lastLon(telemetry.getGpsLon())
            .totalMiles(0.0)
            .build();
    }
    
    private ActiveTrip updateTrip(ActiveTrip trip, NormalizedTelemetry telemetry) {
        // Calculate distance from last point using Haversine formula
        double distance = GeoUtil.calculateDistance(
            trip.getLastLat(), trip.getLastLon(),
            telemetry.getGpsLat(), telemetry.getGpsLon()
        );
        
        // Update trip
        trip.setTotalMiles(trip.getTotalMiles() + distance);
        trip.setLastLat(telemetry.getGpsLat());
        trip.setLastLon(telemetry.getGpsLon());
        trip.setLastUpdateTime(telemetry.getTimestamp());
        
        return trip;
    }
    
    private void endTrip(ActiveTrip activeTrip, NormalizedTelemetry telemetry) {
        // Create completed trip entity
        Trip trip = Trip.builder()
            .id(activeTrip.getTripId())
            .driverId(activeTrip.getDriverId())
            .truckId(activeTrip.getTruckId())
            .deviceId(activeTrip.getDeviceId())
            .startTime(activeTrip.getStartTime())
            .endTime(telemetry.getTimestamp())
            .startLocationLat(activeTrip.getStartLat())
            .startLocationLon(activeTrip.getStartLon())
            .endLocationLat(telemetry.getGpsLat())
            .endLocationLon(telemetry.getGpsLon())
            .totalMiles(activeTrip.getTotalMiles())
            .status(TripStatus.COMPLETED)
            .build();
        
        // Calculate duration
        Duration duration = Duration.between(
            activeTrip.getStartTime(), telemetry.getTimestamp());
        trip.setDurationMinutes((int) duration.toMinutes());
        
        // Persist to database
        tripRepository.save(trip);
        
        log.info("Trip completed: {} miles in {} minutes",
            trip.getTotalMiles(), trip.getDurationMinutes());
    }
}
```

### Haversine Formula Implementation

```java
public class GeoUtil {
    
    private static final double EARTH_RADIUS_MILES = 3958.8;
    
    /**
     * Calculate distance between two GPS points using Haversine formula
     * Returns distance in miles
     */
    public static double calculateDistance(double lat1, double lon1, 
                                          double lat2, double lon2) {
        // Convert to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);
        
        // Haversine formula
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;
        
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.pow(Math.sin(dLon / 2), 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_MILES * c;
    }
}
```

---

## 🎯 Scoring Engine

### Point System

| Violation Type | Points Deducted |
|----------------|-----------------|
| Speeding (Low) | -2 |
| Speeding (Medium) | -5 |
| Speeding (High) | -10 |
| Speeding (Critical) | -15 |
| Harsh Braking (Medium) | -5 |
| Harsh Braking (High) | -10 |
| Harsh Braking (Critical) | -15 |
| Harsh Acceleration | -5 |
| Fatigue Detected | -10 |
| Geofence Violation | -15 |

**Initial Score**: 100 points  
**Minimum Score**: 0 points  
**Maximum Score**: 100 points

### Implementation

```java
@Service
@RequiredArgsConstructor
public class ScoringEngine {
    
    private final DriverRepository driverRepository;
    private final ViolationRepository violationRepository;
    private final TripStateMachine tripStateMachine;
    
    public void processDrivingEvent(DrivingEvent event) {
        // Calculate points to deduct
        int pointsDeducted = calculatePointsDeducted(
            event.getEventType(), 
            event.getSeverity()
        );
        
        // Get current active trip (if any)
        UUID tripId = tripStateMachine.getActiveTripId(event.getDriverId());
        
        // Save violation
        Violation violation = Violation.builder()
            .tripId(tripId)
            .driverId(event.getDriverId())
            .truckId(event.getTruckId())
            .eventType(event.getEventType().name())
            .severity(event.getSeverity().name())
            .locationLat(event.getLocation().getLat())
            .locationLon(event.getLocation().getLon())
            .pointsDeducted(pointsDeducted)
            .metadata(event.getMetadata())
            .description(event.getDescription())
            .timestamp(event.getTimestamp())
            .build();
        
        violationRepository.save(violation);
        
        // Update driver score
        updateDriverScore(event.getDriverId(), pointsDeducted);
        
        // Update trip violation count if trip is active
        if (tripId != null) {
            tripStateMachine.incrementViolationCount(tripId);
        }
    }
    
    private int calculatePointsDeducted(EventType type, Severity severity) {
        return switch (type) {
            case SPEEDING -> switch (severity) {
                case LOW -> 2;
                case MEDIUM -> 5;
                case HIGH -> 10;
                case CRITICAL -> 15;
            };
            case HARSH_BRAKING -> switch (severity) {
                case MEDIUM -> 5;
                case HIGH -> 10;
                case CRITICAL -> 15;
                default -> 5;
            };
            case HARSH_ACCELERATION -> 5;
            case FATIGUE_DETECTED -> 10;
            case GEOFENCE_VIOLATION -> 15;
            default -> 5;
        };
    }
    
    private void updateDriverScore(String driverId, int pointsDeducted) {
        driverRepository.findById(driverId).ifPresentOrElse(
            driver -> {
                int newScore = Math.max(0, driver.getCurrentScore() - pointsDeducted);
                driver.setCurrentScore(newScore);
                driver.setTotalViolations(driver.getTotalViolations() + 1);
                driver.setUpdatedAt(Instant.now());
                driverRepository.save(driver);
                
                log.info("Driver {} score updated: {} (-{} points)",
                    driverId, newScore, pointsDeducted);
            },
            () -> {
                // Create new driver record
                Driver newDriver = Driver.builder()
                    .id(driverId)
                    .currentScore(100 - pointsDeducted)
                    .totalViolations(1)
                    .build();
                driverRepository.save(newDriver);
            }
        );
    }
}
```

---

## 📋 REST API Specification

### 1. Get Driver Score

**Endpoint**: `GET /api/driver/{driverId}/score`

**Response**: `200 OK`

```json
{
  "driver_id": "DRV-ABC-123",
  "name": "John Doe",
  "current_score": 85,
  "total_trips": 45,
  "total_miles": 2340.50,
  "total_violations": 12,
  "score_trend": "DECLINING",
  "last_updated": "2025-12-11T10:30:00Z"
}
```

### 2. Get Driver Trips

**Endpoint**: `GET /api/driver/{driverId}/trips`

**Query Parameters**:
- `limit` (optional): Number of trips to return (default: 20)
- `offset` (optional): Pagination offset (default: 0)
- `status` (optional): Filter by status (ACTIVE, COMPLETED)

**Response**: `200 OK`

```json
{
  "driver_id": "DRV-ABC-123",
  "trips": [
    {
      "trip_id": "550e8400-e29b-41d4-a716-446655440000",
      "truck_id": "TRUCK-789",
      "start_time": "2025-12-11T08:00:00Z",
      "end_time": "2025-12-11T10:30:00Z",
      "duration_minutes": 150,
      "start_location": {
        "lat": 34.0522,
        "lon": -118.2437,
        "address": "Los Angeles, CA"
      },
      "end_location": {
        "lat": 34.1522,
        "lon": -118.1437,
        "address": "Pasadena, CA"
      },
      "total_miles": 52.3,
      "violation_count": 2,
      "status": "COMPLETED"
    }
  ],
  "total_count": 45,
  "page": 1,
  "page_size": 20
}
```

### 3. Get Driver Violations

**Endpoint**: `GET /api/driver/{driverId}/violations`

**Query Parameters**:
- `trip_id` (optional): Filter by specific trip
- `event_type` (optional): Filter by violation type
- `severity` (optional): Filter by severity level
- `start_date` (optional): Filter from date
- `end_date` (optional): Filter to date
- `limit` (optional): Number of violations (default: 50)

**Response**: `200 OK`

```json
{
  "driver_id": "DRV-ABC-123",
  "violations": [
    {
      "violation_id": "650e8400-e29b-41d4-a716-446655440001",
      "trip_id": "550e8400-e29b-41d4-a716-446655440000",
      "truck_id": "TRUCK-789",
      "event_type": "HARSH_BRAKING",
      "severity": "HIGH",
      "location": {
        "lat": 34.0722,
        "lon": -118.2337
      },
      "points_deducted": 10,
      "description": "Harsh braking: -0.45g deceleration at 65.5 MPH",
      "metadata": {
        "acceleration_y": -0.45,
        "speed_at_event": 65.5,
        "braking_distance_estimated": 45.2
      },
      "timestamp": "2025-12-11T09:15:00Z"
    }
  ],
  "total_count": 12,
  "summary": {
    "speeding": 5,
    "harsh_braking": 4,
    "harsh_acceleration": 2,
    "fatigue": 1,
    "geofence_violation": 0
  }
}
```

### 4. Get Trip Details

**Endpoint**: `GET /api/trip/{tripId}`

**Response**: `200 OK`

```json
{
  "trip_id": "550e8400-e29b-41d4-a716-446655440000",
  "driver_id": "DRV-ABC-123",
  "driver_name": "John Doe",
  "truck_id": "TRUCK-789",
  "start_time": "2025-12-11T08:00:00Z",
  "end_time": "2025-12-11T10:30:00Z",
  "duration_minutes": 150,
  "total_miles": 52.3,
  "average_speed": 20.9,
  "start_location": {
    "lat": 34.0522,
    "lon": -118.2437
  },
  "end_location": {
    "lat": 34.1522,
    "lon": -118.1437
  },
  "violations": [
    {
      "event_type": "HARSH_BRAKING",
      "severity": "HIGH",
      "timestamp": "2025-12-11T09:15:00Z",
      "location": {
        "lat": 34.0722,
        "lon": -118.2337
      }
    }
  ],
  "status": "COMPLETED"
}
```

---

## 🔧 Implementation Guide

### Controller Example

```java
@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverController {
    
    private final DriverService driverService;
    
    @GetMapping("/{driverId}/score")
    public ResponseEntity<DriverScoreResponse> getDriverScore(
            @PathVariable String driverId) {
        
        return driverService.getDriverScore(driverId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{driverId}/trips")
    public ResponseEntity<DriverTripsResponse> getDriverTrips(
            @PathVariable String driverId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String status) {
        
        DriverTripsResponse response = driverService.getDriverTrips(
            driverId, limit, offset, status);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{driverId}/violations")
    public ResponseEntity<DriverViolationsResponse> getDriverViolations(
            @PathVariable String driverId,
            @RequestParam(required = false) UUID tripId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String severity,
            @RequestParam(defaultValue = "50") int limit) {
        
        DriverViolationsResponse response = driverService.getDriverViolations(
            driverId, tripId, eventType, severity, limit);
        
        return ResponseEntity.ok(response);
    }
}
```

### Configuration: application.yml

```yaml
spring:
  application:
    name: driver-scoring-service
  
  datasource:
    url: jdbc:postgresql://localhost:5432/fleet_management
    username: fleet_user
    password: fleet_password
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: driver-scoring-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      
server:
  port: 8083

logging:
  level:
    com.fleet.scoring: DEBUG
    org.hibernate.SQL: DEBUG
```

---

## 🧪 Testing

### Testing Trip State Machine

```java
@Test
void shouldStartTripOnIgnitionOn() {
    NormalizedTelemetry telemetry = NormalizedTelemetry.builder()
        .driverId("DRV-TEST-001")
        .ignitionStatus("ON")
        .gpsLat(34.0522)
        .gpsLon(-118.2437)
        .build();
    
    tripStateMachine.processNormalizedTelemetry(telemetry);
    
    assertTrue(tripStateMachine.hasActiveTrip("DRV-TEST-001"));
}

@Test
void shouldCalculateDistanceBetweenPoints() {
    double lat1 = 34.0522;
    double lon1 = -118.2437;
    double lat2 = 34.0622; // ~0.7 miles north
    double lon2 = -118.2437;
    
    double distance = GeoUtil.calculateDistance(lat1, lon1, lat2, lon2);
    
    assertThat(distance).isBetween(0.65, 0.75);
}
```

---

## 📚 Teaching Discussion Points

### 1. Event Sourcing Pattern
- State is rebuilt from stream of events
- No direct updates - all changes via events
- Enables audit trail and replay

### 2. State Management
- In-memory state (active trips) vs persistent state (completed trips)
- Trade-offs: speed vs durability
- Handling service restarts

### 3. Database vs Event Stream
- Database is the "materialized view" of event stream
- Could rebuild entire database from Kafka topics
- Separation of command (events) and query (REST API)

### 4. CQRS Pattern
- Write side: Kafka consumers updating state
- Read side: REST API querying database
- Different models for writes vs reads

---

## 🚀 Deployment Checklist

- [ ] PostgreSQL database created and migrated
- [ ] Kafka topics created
- [ ] Service 1 (Telemetry Ingestion) running
- [ ] Service 2 (Event Processing) running
- [ ] Service 3 (Driver Scoring) running
- [ ] All services connected to Kafka
- [ ] Database populated with sample data
- [ ] REST API endpoints responding
- [ ] End-to-end data flow verified

---

## 🎓 Final Project Exercise

Create a simple web dashboard that:
1. Shows driver score with visual indicator (red/yellow/green)
2. Displays trip history in a table
3. Shows violations on a map (use Leaflet.js or Google Maps)
4. Real-time updates as new violations occur

This completes the full event-driven microservices architecture!
