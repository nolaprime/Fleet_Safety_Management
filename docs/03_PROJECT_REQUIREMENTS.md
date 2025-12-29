# Fleet Management System - Project Requirements

## Project Overview

You will build a **Fleet Management System** that tracks and analyzes truck telemetry data in real-time using Apache Kafka and Spring Boot microservices.

### Business Context

A logistics company operates a fleet of trucks. Each truck is equipped with sensors that continuously send telemetry data (speed, location, fuel level, etc.). The company needs to:

1. **Collect** telemetry data from trucks in real-time
2. **Process** events and detect issues (speeding, maintenance alerts)
3. **Score** driver performance based on their driving behavior

---

## System Architecture

```
Trucks (Sensors)
       ↓
  [REST API]
       ↓
┌─────────────────────────────────────────────────────────────┐
│  Service 1: Telemetry Ingestion Service (Producer)          │
│  - Receives telemetry data via REST API                     │
│  - Validates and publishes to Kafka                         │
└──────────────────────┬──────────────────────────────────────┘
                       ↓
              ┌────────────────┐
              │  Kafka Topic:  │
              │ "raw-telemetry"│
              └────────┬───────┘
                       ↓
        ┌──────────────┴──────────────┐
        ↓                             ↓
┌───────────────────────┐    ┌───────────────────────┐
│ Service 2:            │    │ Service 3:            │
│ Event Processing      │    │ Driver Scoring        │
│ Service (Consumer)    │    │ Service (Consumer)    │
│ - Detects violations  │    │ - Calculates scores   │
│ - Enriches data       │    │ - Stores in database  │
│ - Publishes events    │    │ - Provides analytics  │
└──────────┬────────────┘    └───────────────────────┘
           ↓
    ┌──────────────┐
    │ Kafka Topic: │
    │ "violations" │
    └──────────────┘
```

---

## Service 1: Telemetry Ingestion Service

### Purpose
Act as the **entry point** for all truck telemetry data. This service receives data from trucks and publishes it to Kafka for downstream processing.

### Responsibilities

1. **Expose REST API** to receive telemetry data
2. **Validate** incoming data (required fields, reasonable values)
3. **Enrich** data with timestamp if not provided
4. **Publish** telemetry to Kafka topic `raw-telemetry`
5. **Log** ingestion metrics for monitoring

### API Specification

#### Endpoint: POST /api/telemetry/ingest

**Request Body:**
```json
{
  "truckId": "TRUCK-001",
  "driverId": "DRV-12345",
  "timestamp": 1702678800000,
  "location": {
    "latitude": 34.0522,
    "longitude": -118.2437
  },
  "speed": 75.5,
  "fuelLevel": 68.3,
  "engineTemp": 92.0,
  "tirePressure": {
    "frontLeft": 32.5,
    "frontRight": 32.3,
    "rearLeft": 80.0,
    "rearRight": 79.5
  }
}
```

**Response (Success - 200 OK):**
```json
{
  "status": "ACCEPTED",
  "message": "Telemetry data received successfully",
  "truckId": "TRUCK-001",
  "timestamp": 1702678800000
}
```

**Response (Validation Error - 400 Bad Request):**
```json
{
  "status": "REJECTED",
  "message": "Validation failed: speed must be between 0 and 200",
  "errors": [
    "speed: must be between 0 and 200"
  ]
}
```

### Data Model

**TelemetryData** (to be published to Kafka):
```java
{
  String truckId;           // Required, format: TRUCK-XXX
  String driverId;          // Required, format: DRV-XXXXX
  Long timestamp;           // Unix timestamp in milliseconds
  Location location;        // GPS coordinates
  Double speed;             // km/h (0-200)
  Double fuelLevel;         // percentage (0-100)
  Double engineTemp;        // Celsius (0-150)
  TirePressure tirePressure; // PSI values for each tire
}
```

### Validation Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| truckId | Required, matches pattern `TRUCK-\d{3}` | "Invalid truck ID format" |
| driverId | Required, matches pattern `DRV-\d{5}` | "Invalid driver ID format" |
| speed | 0 ≤ speed ≤ 200 | "Speed must be between 0 and 200 km/h" |
| fuelLevel | 0 ≤ fuelLevel ≤ 100 | "Fuel level must be between 0 and 100%" |
| engineTemp | 0 ≤ engineTemp ≤ 150 | "Engine temperature must be between 0 and 150°C" |
| tirePressure | All values: 20 ≤ pressure ≤ 120 | "Tire pressure must be between 20 and 120 PSI" |

### Expected Behavior

1. **Happy Path:**
   - Receive telemetry → Validate → Add timestamp if missing → Publish to Kafka → Return success

2. **Validation Failure:**
   - Receive telemetry → Validate → Return 400 error with details → Do NOT publish to Kafka

3. **Kafka Unavailable:**
   - Receive telemetry → Validate → Attempt to publish → Log error → Return 503 Service Unavailable

4. **Logging:**
   ```
   INFO: Received telemetry from truck TRUCK-001
   INFO: Published telemetry to Kafka: truckId=TRUCK-001, speed=75.5 km/h
   ERROR: Failed to publish to Kafka: [error details]
   ```

### Performance Requirements

- Handle **at least 100 requests per second**
- API response time: < 100ms (excluding Kafka publish time)
- Use **asynchronous** Kafka publishing (fire-and-forget pattern)

---

## Service 2: Event Processing Service

### Purpose
**Consume** telemetry data from Kafka, detect **violations and anomalies**, enrich data, and publish **events** for further processing.

### Responsibilities

1. **Consume** messages from `raw-telemetry` topic
2. **Detect violations:**
   - Speeding (speed > 80 km/h)
   - Low fuel (fuelLevel < 15%)
   - High engine temperature (engineTemp > 110°C)
   - Low tire pressure (any tire < 28 PSI)
3. **Enrich** data with violation details
4. **Publish** violation events to `violations` topic
5. **Log** all processed telemetry and violations

### Violation Detection Rules

#### 1. Speeding Violation
```
Condition: speed > 80 km/h
Severity: HIGH if speed > 100, MEDIUM if 80 < speed ≤ 100
Message: "Truck [truckId] speeding at [speed] km/h (Driver: [driverId])"
```

#### 2. Low Fuel Alert
```
Condition: fuelLevel < 15%
Severity: CRITICAL if fuel < 5%, HIGH if 5 ≤ fuel < 15%
Message: "Truck [truckId] low fuel: [fuelLevel]%"
```

#### 3. High Engine Temperature
```
Condition: engineTemp > 110°C
Severity: CRITICAL if temp > 120°C, HIGH if 110 < temp ≤ 120°C
Message: "Truck [truckId] high engine temperature: [engineTemp]°C"
```

#### 4. Low Tire Pressure
```
Condition: Any tire pressure < 28 PSI
Severity: HIGH
Message: "Truck [truckId] low tire pressure: [tire_position]=[pressure] PSI"
```


### Data Model

**ViolationEvent** (to be published to Kafka):
```java
{
  String violationId;      // UUID
  String truckId;          // From telemetry
  String driverId;         // From telemetry
  String violationType;    // SPEEDING, LOW_FUEL, HIGH_TEMP, LOW_TIRE_PRESSURE
  String severity;         // CRITICAL, HIGH, MEDIUM, LOW
  String message;          // Human-readable violation description
  TelemetryData originalData; // The full telemetry that triggered violation
  Long detectedAt;         // Timestamp when violation was detected
}
```

### Expected Behavior

1. **Normal Telemetry (No Violations):**
   ```
   Consume telemetry → Check all rules → No violations found → Log: "Processed telemetry from TRUCK-001: No violations"
   ```

2. **Single Violation Detected:**
   ```
   Consume telemetry → Detect speeding violation → Create ViolationEvent → Publish to "violations" topic → Log violation
   ```

3. **Multiple Violations Detected:**
   ```
   Consume telemetry → Detect speeding AND low tire pressure → Create 2 ViolationEvents → Publish both → Log both
   ```

4. **Logging Examples:**
   ```
   INFO: Consumed telemetry: truckId=TRUCK-001, speed=75.5 km/h
   WARN: VIOLATION DETECTED - Speeding: TRUCK-001 at 105.5 km/h (Driver: DRV-12345)
   CRITICAL: VIOLATION DETECTED - Low Fuel: TRUCK-002 at 3.2%
   INFO: Published violation event: violationId=abc-123, type=SPEEDING
   ```

### Performance Requirements

- Process **at least 500 messages per second**
- Violation detection latency: < 50ms per message
- Use **batch processing** if applicable to improve throughput

---

## Service 3: Driver Scoring Service

### Purpose
**Analyze** driver behavior over time and calculate **driver performance scores** based on their violation history and driving patterns.

### Responsibilities

1. **Consume** messages from `violations` topic
2. **Store** violations in PostgreSQL database
3. **Calculate** driver scores based on violation history
4. **Provide REST API** to query driver scores and statistics
5. **Update** scores in real-time as new violations occur

### Scoring Algorithm

#### Base Score: 100 points

Each driver starts with a perfect score of 100. Points are **deducted** based on violations within the **last 30 days**.

#### Point Deductions

| Violation Type | Severity | Points Deducted |
|---------------|----------|-----------------|
| SPEEDING | MEDIUM (80-100 km/h) | -2 points |
| SPEEDING | HIGH (>100 km/h) | -5 points |
| LOW_FUEL | HIGH (5-15%) | -1 point |
| LOW_FUEL | CRITICAL (<5%) | -3 points |
| HIGH_TEMP | HIGH (110-120°C) | -3 points |
| HIGH_TEMP | CRITICAL (>120°C) | -5 points |
| LOW_TIRE_PRESSURE | HIGH | -2 points |

#### Score Calculation Example

```
Driver DRV-12345 in last 30 days:
- 3 MEDIUM speeding violations: 3 × -2 = -6 points
- 1 HIGH speeding violation: 1 × -5 = -5 points
- 2 LOW tire pressure: 2 × -2 = -4 points

Final Score: 100 - 6 - 5 - 4 = 85 points
```

#### Score Categories

| Score Range | Category | Description |
|-------------|----------|-------------|
| 90-100 | EXCELLENT | Model driver, very few violations |
| 75-89 | GOOD | Generally safe driver, minor issues |
| 60-74 | AVERAGE | Moderate violations, needs improvement |
| 40-59 | POOR | Frequent violations, requires training |
| 0-39 | CRITICAL | Serious safety risk, immediate action needed |

### Database Schema

#### Table: violations
```sql
CREATE TABLE violations (
    id UUID PRIMARY KEY,
    truck_id VARCHAR(50) NOT NULL,
    driver_id VARCHAR(50) NOT NULL,
    violation_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    message TEXT,
    detected_at TIMESTAMP NOT NULL,
    speed DOUBLE PRECISION,
    fuel_level DOUBLE PRECISION,
    engine_temp DOUBLE PRECISION,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_driver_id ON violations(driver_id);
CREATE INDEX idx_detected_at ON violations(detected_at);
```

#### Table: driver_scores
```sql
CREATE TABLE driver_scores (
    driver_id VARCHAR(50) PRIMARY KEY,
    current_score INTEGER NOT NULL DEFAULT 100,
    score_category VARCHAR(20) NOT NULL,
    total_violations INTEGER DEFAULT 0,
    last_violation_date TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### API Specification

#### 1. Get Driver Score

**Endpoint:** GET /api/drivers/{driverId}/score

**Response:**
```json
{
  "driverId": "DRV-12345",
  "currentScore": 85,
  "scoreCategory": "GOOD",
  "totalViolations": 6,
  "lastViolationDate": "2025-12-15T10:30:00Z",
  "violationBreakdown": {
    "SPEEDING": 4,
    "LOW_TIRE_PRESSURE": 2,
    "LOW_FUEL": 0,
    "HIGH_TEMP": 0
  }
}
```

#### 2. Get Driver Violation History

**Endpoint:** GET /api/drivers/{driverId}/violations?days=30

**Response:**
```json
{
  "driverId": "DRV-12345",
  "periodDays": 30,
  "totalViolations": 6,
  "violations": [
    {
      "violationId": "abc-123",
      "truckId": "TRUCK-001",
      "violationType": "SPEEDING",
      "severity": "HIGH",
      "message": "Truck TRUCK-001 speeding at 105.5 km/h",
      "detectedAt": "2025-12-14T15:30:00Z",
      "speed": 105.5,
      "location": {
        "latitude": 34.0522,
        "longitude": -118.2437
      }
    }
    // ... more violations
  ]
}
```

#### 3. Get Top Drivers (Leaderboard)

**Endpoint:** GET /api/drivers/leaderboard?limit=10

**Response:**
```json
{
  "leaderboard": [
    {
      "driverId": "DRV-99999",
      "currentScore": 100,
      "scoreCategory": "EXCELLENT",
      "totalViolations": 0
    },
    {
      "driverId": "DRV-88888",
      "currentScore": 98,
      "scoreCategory": "EXCELLENT",
      "totalViolations": 1
    }
    // ... more drivers
  ]
}
```

#### 4. Get Bottom Drivers (Need Improvement)

**Endpoint:** GET /api/drivers/bottom?limit=10

**Response:**
```json
{
  "driversNeedingAttention": [
    {
      "driverId": "DRV-11111",
      "currentScore": 35,
      "scoreCategory": "CRITICAL",
      "totalViolations": 25,
      "recommendedAction": "Immediate suspension and retraining required"
    }
    // ... more drivers
  ]
}
```

### Expected Behavior

1. **New Violation Received:**
   ```
   Consume violation event → Store in database → Recalculate driver score → Update driver_scores table → Log update
   ```

2. **Score Calculation:**
   ```
   Query violations for driver in last 30 days → Sum point deductions → Calculate score (100 - deductions) → Determine category → Store result
   ```

3. **API Query:**
   ```
   Receive GET request → Query database → Calculate statistics → Return JSON response
   ```

4. **Logging Examples:**
   ```
   INFO: Received violation: violationId=abc-123, driverId=DRV-12345, type=SPEEDING
   INFO: Stored violation in database: TRUCK-001, SPEEDING, HIGH severity
   INFO: Updated driver score: DRV-12345, 90 → 85 points, category: GOOD
   ```

### Performance Requirements

- Handle **at least 200 violations per second**
- API response time: < 200ms
- Score calculation: < 50ms per driver
- Database queries: Properly indexed for fast lookups

---

## Data Flow Example

### Complete End-to-End Scenario

**Step 1: Truck Sends Telemetry**
```
Driver DRV-12345 is driving TRUCK-001 at 105 km/h
```

**Step 2: Telemetry Ingestion Service**
```
POST /api/telemetry/ingest
{
  "truckId": "TRUCK-001",
  "driverId": "DRV-12345",
  "speed": 105.5,
  "fuelLevel": 68.3,
  "engineTemp": 92.0,
  // ... other fields
}

→ Validates data
→ Publishes to Kafka topic "raw-telemetry"
→ Returns 200 OK
```

**Step 3: Event Processing Service**
```
Consumes from "raw-telemetry"
→ Detects: speed (105.5) > 80 km/h → SPEEDING VIOLATION (HIGH severity)
→ Creates ViolationEvent:
   {
     "violationType": "SPEEDING",
     "severity": "HIGH",
     "message": "Truck TRUCK-001 speeding at 105.5 km/h (Driver: DRV-12345)",
     ...
   }
→ Publishes to Kafka topic "violations"
→ Logs: "WARN: VIOLATION DETECTED - Speeding: TRUCK-001 at 105.5 km/h"
```

**Step 4: Driver Scoring Service**
```
Consumes from "violations"
→ Stores violation in PostgreSQL
→ Recalculates driver score:
   - Previous score: 90 points
   - Deduction: -5 points (HIGH speeding)
   - New score: 85 points
   - Category: GOOD (75-89 range)
→ Updates driver_scores table
→ Logs: "INFO: Updated driver score: DRV-12345, 90 → 85 points"
```

**Step 5: Manager Queries Dashboard**
```
GET /api/drivers/DRV-12345/score

Response:
{
  "driverId": "DRV-12345",
  "currentScore": 85,
  "scoreCategory": "GOOD",
  "totalViolations": 6,
  "lastViolationDate": "2025-12-15T10:30:00Z",
  ...
}
```

---

## Project Deliverables

### What You Need to Build

1. **Three Spring Boot Microservices:**
   - Telemetry Ingestion Service (Producer)
   - Event Processing Service (Consumer → Producer)
   - Driver Scoring Service (Consumer + REST API)

2. **Data Models:**
   - TelemetryData
   - ViolationEvent
   - DriverScore
   - Supporting models (Location, TirePressure, etc.)

3. **Kafka Integration:**
   - Producer configurations
   - Consumer configurations
   - Topic management

4. **Database:**
   - PostgreSQL schema (violations, driver_scores tables)
   - JPA entities and repositories
   - Proper indexing

5. **REST APIs:**
   - Telemetry ingestion endpoint (Service 1)
   - Driver query endpoints (Service 3)

6. **Testing:**
   - Unit tests for business logic
   - Integration tests for Kafka producers/consumers
   - API endpoint tests

---

## Evaluation Criteria

Your project will be evaluated on:

1. **Functionality (40%)**
   - All three services work correctly
   - Violations are detected accurately
   - Driver scores calculated properly
   - APIs return correct responses

2. **Code Quality (30%)**
   - Clean, readable code
   - Proper error handling
   - Meaningful logging
   - Good separation of concerns

3. **Kafka Integration (20%)**
   - Correct producer/consumer configuration
   - Proper serialization/deserialization
   - Error handling for Kafka failures
   - Efficient message processing

4. **Documentation (10%)**
   - README with setup instructions
   - API documentation
   - Code comments for complex logic
   - Testing instructions

---

## Getting Started

1. **Set up Kafka and PostgreSQL:**
   ```bash
   cd docker
   docker-compose up -d
   ```

2. **Create the three service skeletons:**
   - Use Spring Initializr or Maven archetype
   - Add dependencies: Spring Boot, Spring Kafka, PostgreSQL, Lombok

3. **Start with Service 1 (Telemetry Ingestion):**
   - Create REST controller
   - Implement validation
   - Configure Kafka producer
   - Test with curl or Postman

4. **Move to Service 2 (Event Processing):**
   - Configure Kafka consumer
   - Implement violation detection logic
   - Configure Kafka producer for violations
   - Test by sending telemetry to Service 1

5. **Finish with Service 3 (Driver Scoring):**
   - Set up PostgreSQL database
   - Create JPA entities and repositories
   - Configure Kafka consumer for violations
   - Implement scoring algorithm
   - Create REST APIs
   - Test end-to-end flow

---

## Tips for Success

- **Start simple:** Get basic message flow working first, then add features
- **Test incrementally:** Test each service independently before integration
- **Use Kafka UI:** Monitor messages in topics using http://localhost:8080
- **Check logs:** Use proper logging to debug issues
- **Handle errors:** Don't let one bad message crash your consumer
- **Use constants:** Define violation thresholds as configurable properties
- **Think real-time:** Remember, this is a streaming system - data flows continuously

---

## Resources

- **Kafka Basics:** `01_KAFKA_INTRODUCTION.md`
- **Spring Kafka:** `02_SPRING_KAFKA_GUIDE.md`
- **Kafka Commands:** `KAFKA_CHEAT_SHEET.md`
- **Testing Guide:** `TESTING_GUIDE.md`
- **Troubleshooting:** `TROUBLESHOOTING.md`

Good luck! 🚚 🚀
