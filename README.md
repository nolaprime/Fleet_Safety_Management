# Fleet Management System - Project Summary

## 📦 What Has Been Created

This is a **complete educational project** for teaching event-driven microservices architecture using Spring Boot and Apache Kafka. The project simulates a real-world fleet management system that processes telemetry data from trucks in real-time.

---

## 🚀 Quick Start (Primary Method)

**Run the entire system with one command:**

```bash
./build-and-run-docker.sh
```

This automated script:
1. ✅ Checks prerequisites (Docker, Maven, Java 17)
2. ✅ Builds Maven artifacts
3. ✅ Builds Docker images
4. ✅ Starts all services (Kafka, PostgreSQL, microservices)
5. ✅ Creates Kafka topics
6. ✅ Displays service URLs and test commands

**Test the system:**

```bash
./test-kafka-demo.sh
```

This sends test telemetry with various scenarios (normal, violations) to verify the complete data flow.

---

## 📁 Project Structure

```
FleetManagementSystem/
├── README.md                          # Main project overview
├── pom.xml                            # Parent Maven configuration
├── build-and-run-docker.sh           # 🚀 PRIMARY ENTRY POINT
├── test-kafka-demo.sh                # 🧪 PRIMARY TESTING SCRIPT
├── mvnw.sh                            # Maven wrapper for Java 17
│
├── docs/                              # Complete documentation
│   ├── 00_START_HERE.md              # Master guide (start here!)
│   ├── 01_KAFKA_INTRODUCTION.md      # Kafka fundamentals for beginners
│   ├── 02_SPRING_KAFKA_GUIDE.md      # Spring Kafka with examples
│   ├── 03_PROJECT_REQUIREMENTS.md    # Complete specifications
│   ├── LEARNING_ROADMAP.md           # Week-by-week implementation guide
│   ├── KAFKA_CHEAT_SHEET.md          # Quick command reference
│   ├── TESTING_GUIDE.md              # Testing strategies
│   ├── TROUBLESHOOTING.md            # Common issues and solutions
│   └── sample-telemetry.json         # Sample test data
│
├── docker/                            # Docker infrastructure
│   ├── docker-compose.yml            # Kafka, PostgreSQL, UIs, Microservices
│   └── init-db.sql                   # Database schema
│
├── telemetry-ingestion-service/      # Service 1 (Producer)
│   ├── Dockerfile                    # Docker build configuration
│   ├── src/main/java/com/fleet/telemetry/
│   │   ├── TelemetryIngestionApplication.java
│   │   ├── controller/TelemetryController.java
│   │   ├── service/TelemetryProducer.java
│   │   ├── config/KafkaProducerConfig.java
│   │   └── model/TelemetryData.java
│   └── src/main/resources/
│       ├── application.properties           # Local config
│       └── application-docker.properties    # Docker config
│
├── event-processing-service/         # Service 2 (Consumer + Producer)
│   ├── Dockerfile                    # Docker build configuration
│   ├── src/main/java/com/fleet/processor/
│   │   ├── EventProcessingApplication.java
│   │   ├── service/TelemetryConsumer.java
│   │   ├── config/KafkaConsumerConfig.java
│   │   └── model/TelemetryData.java
│   └── src/main/resources/
│       ├── application.properties           # Local config
│       └── application-docker.properties    # Docker config
│
└── driver-scoring-service/           # Service 3 (Consumer + REST API) [TODO]
    └── (To be implemented by students)
```

---

## 🎯 Architecture Overview

```
Trucks (IoT Devices)
        │
        │ HTTP POST (Telemetry Data)
        ▼
┌──────────────────────────────┐
│  Service 1: Telemetry        │  Port 8081 (Docker)
│  Ingestion Service           │  
│  - REST API                  │  → Validates data
│  - Kafka Producer            │  → Publishes to Kafka
└────────────┬─────────────────┘
             │
             ▼
      [raw-telemetry] Topic
             │
             │ Consumes
             ▼
┌──────────────────────────────┐
│  Service 2: Event            │  (Consumer only)
│  Processing Service          │
│  - Kafka Consumer            │  → Detects violations
│  - Business Logic            │  → Logs events
└──────────────────────────────┘

[Service 3: Driver Scoring - To be implemented by students]
```

**Current Implementation:** Services 1 & 2  
**Student Task:** Implement Service 3 following requirements in `docs/03_PROJECT_REQUIREMENTS.md`

---

## 🎓 Key Learning Concepts

### 1. Event-Driven Architecture
- **Asynchronous messaging** between services via Kafka
- **Decoupling** - services communicate through topics, not direct calls
- **Scalability** - each service scales independently
- **Resilience** - message persistence and replay capability

### 2. Apache Kafka Core Concepts
- **Producers** - Publishing messages to topics (Service 1)
- **Consumers** - Reading messages from topics (Service 2)
- **Topics & Partitions** - Message organization and parallelism
- **Consumer Groups** - Load balancing and fault tolerance
- **JSON Serialization** - Converting Java objects to/from bytes

### 3. Spring Boot & Spring Kafka
- **Dependency Injection** - Spring IoC container
- **KafkaTemplate** - Simplified producer API
- **@KafkaListener** - Annotation-based consumers
- **Configuration Properties** - Externalized configuration
- **Docker Profiles** - Environment-specific settings

### 4. Docker & Containerization
- **Multi-stage builds** - Optimized image sizes
- **Docker Compose** - Service orchestration
- **Health checks** - Service dependency management
- **Networking** - Inter-container communication

---

## 📚 Learning Path

### Phase 1: Understand Kafka (Week 1)
1. Read `docs/01_KAFKA_INTRODUCTION.md` - Kafka fundamentals
2. Read `docs/02_SPRING_KAFKA_GUIDE.md` - Spring integration with examples
3. Set up environment with `./build-and-run-docker.sh`
4. Test with `./test-kafka-demo.sh`

### Phase 2: Study the Project (Week 1-2)
5. Read `docs/03_PROJECT_REQUIREMENTS.md` - Complete specifications
6. Review implemented Services 1 & 2 code
7. Understand the data flow and architecture

### Phase 3: Implement Service 3 (Week 2-4)
8. Follow `docs/LEARNING_ROADMAP.md` for guidance
9. Implement Driver Scoring Service per requirements
10. Test end-to-end with all three services

---

## �️ Development Workflow

### Primary Method: Docker (Recommended)

```bash
# Build and run everything
./build-and-run-docker.sh

# View logs
cd docker
docker-compose logs -f

# Stop services
docker-compose down

# Fresh start (removes all data)
docker-compose down -v
docker-compose up -d
```

### Alternative: Local Development

```bash
# Start infrastructure only
cd docker
docker-compose up -d zookeeper kafka postgres

# Build services
mvn clean package -DskipTests

# Run each service in separate terminal
cd telemetry-ingestion-service && mvn spring-boot:run
cd event-processing-service && mvn spring-boot:run
```

---

## 🧪 Testing

### Automated Testing (Primary Method)

```bash
# Test the complete flow
./test-kafka-demo.sh
```

This script sends 5 test messages with different scenarios:
1. Normal telemetry (no violations)
2. Speeding violation (speed > 80 km/h)
3. Multiple data points from same truck
4. Different trucks
5. Edge cases

### Manual Testing

```bash
# Send normal telemetry
curl -X POST http://localhost:8081/api/telemetry/ingest \
  -H "Content-Type: application/json" \
  -d '{"truckId": "TRUCK-001", "driverId": "DRV-12345", "speed": 75.5}'

# Send speeding violation
curl -X POST http://localhost:8081/api/telemetry/ingest \
  -H "Content-Type: application/json" \
  -d '{"truckId": "TRUCK-002", "driverId": "DRV-67890", "speed": 105.0}'
```

### Monitoring

- **Kafka UI:** http://localhost:8080
  - View topics and messages
  - Monitor consumer groups
  - Check message flow

- **Service Logs:**
  ```bash
  docker-compose logs -f telemetry-ingestion-service
  docker-compose logs -f event-processing-service
  ```

- **pgAdmin:** http://localhost:5050 (admin@fleet.com / admin)
  - View database tables
  - Check stored violations

---

## 📊 Technologies Used

| Category | Technology |
|----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.0 |
| Messaging | Apache Kafka 3.6 |
| Database | PostgreSQL 15 |
| Build Tool | Maven 3.8+ |
| Containerization | Docker & Docker Compose |
| Testing | JUnit 5, Mockito, TestContainers |
| Monitoring | Spring Actuator, Kafka UI |

---

## 📚 Documentation Guide

### Primary Learning Path (Read in Order)
1. **[docs/00_START_HERE.md](docs/00_START_HERE.md)** - Master guide and project overview
2. **[docs/01_KAFKA_INTRODUCTION.md](docs/01_KAFKA_INTRODUCTION.md)** - Kafka fundamentals for beginners
3. **[docs/02_SPRING_KAFKA_GUIDE.md](docs/02_SPRING_KAFKA_GUIDE.md)** - Spring Kafka with working examples
4. **[docs/03_PROJECT_REQUIREMENTS.md](docs/03_PROJECT_REQUIREMENTS.md)** - Complete project specifications
5. **[docs/LEARNING_ROADMAP.md](docs/LEARNING_ROADMAP.md)** - Week-by-week implementation guide

### Reference Documentation
- **[docs/KAFKA_CHEAT_SHEET.md](docs/KAFKA_CHEAT_SHEET.md)** - Quick command reference
- **[docs/TESTING_GUIDE.md](docs/TESTING_GUIDE.md)** - Testing strategies
- **[docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)** - Common issues and solutions
- **[docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md)** - REST API specifications

### Quick Reference
- **[QUICK_START.md](QUICK_START.md)** - One-page quick reference
- **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)** - This document

---

## 🎯 Learning Outcomes

After completing this project, students will:

✅ **Understand event-driven architecture** principles  
✅ **Build Kafka producers and consumers** in Spring Boot  
✅ **Design microservices** that communicate asynchronously  
✅ **Implement real-time data processing** pipelines  
✅ **Deploy containerized applications** with Docker  
✅ **Test distributed systems** effectively  
✅ **Apply best practices** for production systems

---

## 🚀 Key Scripts

### 1. `build-and-run-docker.sh` - Primary Entry Point
**Purpose:** One-command deployment of entire system

**What it does:**
- Checks prerequisites (Docker, Java 17, Maven)
- Builds Maven artifacts for both services
- Builds Docker images
- Starts all infrastructure (Kafka, Zookeeper, PostgreSQL)
- Starts microservices
- Creates Kafka topics automatically
- Displays service URLs and next steps

**Usage:**
```bash
./build-and-run-docker.sh
```

**When to use:** 
- First time setup
- After code changes (rebuilds everything)
- Production-like deployment
- Demo/presentation

---

### 2. `test-kafka-demo.sh` - Primary Testing Script
**Purpose:** Automated testing of the complete data flow

**What it does:**
- Sends 5 test telemetry messages with different scenarios:
  - Normal telemetry (no violations)
  - Speeding violation (speed = 85 km/h)
  - High-speed violation (speed = 110 km/h)
  - Different trucks and drivers
  - Edge cases
- Displays expected output in logs
- Shows where to look for results

**Usage:**
```bash
./test-kafka-demo.sh
```

**When to use:**
- After starting services
- To verify system is working
- To see violation detection in action
- For demonstrations

---

## 💡 Recommended Workflow

### Initial Setup (First Time)
```bash
# 1. Build and run everything
./build-and-run-docker.sh

# 2. Wait for services to start (30-60 seconds)

# 3. Test the system
./test-kafka-demo.sh

# 4. View logs to see violation detection
cd docker
docker-compose logs -f event-processing-service
```

### After Code Changes
```bash
# Rebuild and restart
./build-and-run-docker.sh

# Test
./test-kafka-demo.sh
```

### Development/Debugging
```bash
# Start infrastructure only
cd docker
docker-compose up -d zookeeper kafka postgres kafka-ui pgadmin

# Run services locally (in separate terminals)
cd telemetry-ingestion-service && mvn spring-boot:run
cd event-processing-service && mvn spring-boot:run

# Test
./test-kafka-demo.sh
```

# Verify PostgreSQL
psql -h localhost -U fleet_user -d fleet_management
```

### Phase 2: Service 1 (Week 1)
1. Create Spring Boot project
2. Add REST endpoint
3. Integrate Kafka producer
4. Write tests
5. Document learnings

### Phase 3: Service 2 (Week 2)
1. Create Kafka consumer
2. Implement normalization
3. Build rule engine
4. Publish to two topics
5. Test thoroughly

### Phase 4: Service 3 (Week 3)
1. Design database schema
2. Implement trip state machine
3. Build scoring engine
4. Create REST APIs
5. End-to-end testing

---

## 🎬 Demo Scenarios

### Scenario 1: Normal Trip
```
1. Start trip (ignition ON)
2. Drive normally
3. End trip (ignition OFF)
Expected: Trip recorded, score unchanged
```

### Scenario 2: Violation Detection
```
1. Start trip
2. Trigger speeding (85 MPH in 55 zone)
3. Trigger harsh braking (-0.5g)
4. End trip
Expected: Score reduced by 15 points, 2 violations recorded
```

### Scenario 3: Fatigue Detection
```
1. Drive for 4+ hours without break
Expected: Fatigue violation, 10 points deducted
```

---

## 🔍 Monitoring & Debugging

### Tools Available
- **Kafka UI**: http://localhost:8080
- **pgAdmin**: http://localhost:5050
- **Actuator**: http://localhost:808X/actuator

### Key Metrics
- Consumer lag (check with `kafka-consumer-groups`)
- Message throughput (Kafka UI)
- Database query performance (pgAdmin)
- JVM metrics (Actuator)

---

## 🚀 Future Enhancements

### Level 1: Core Features
- [ ] Dead Letter Queue for failed messages
- [ ] Exactly-once semantics
- [ ] Idempotent consumers

### Level 2: Production Features
- [ ] Security (OAuth2, JWT)
- [ ] Distributed tracing (Jaeger)
- [ ] Centralized logging (ELK)
- [ ] Monitoring (Prometheus + Grafana)

### Level 3: Advanced Features
- [ ] Real-time WebSocket dashboard
- [ ] Machine learning for anomaly detection
- [ ] Multi-region deployment
- [ ] Event replay capability

---

## 📖 Additional Resources

### Books
- "Kafka: The Definitive Guide" - Neha Narkhede
- "Building Event-Driven Microservices" - Adam Bellemare
- "Designing Data-Intensive Applications" - Martin Kleppmann

### Online Courses
- Confluent Kafka Tutorials
- Spring Boot Masterclass
- Microservices Patterns (Udemy)

### Documentation
- [Apache Kafka Docs](https://kafka.apache.org/documentation/)
- [Spring Kafka Reference](https://docs.spring.io/spring-kafka/reference/)
- [Spring Boot Guides](https://spring.io/guides)

---

## 🤝 Support

### For Issues
1. Check [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)
2. Search documentation
3. Check Kafka/PostgreSQL logs
4. Use debugging tools

### For Questions
- Review architecture diagrams
- Read service specifications
- Check discussion points in docs
- Test hypotheses with experiments

---

## ✅ Project Completion Checklist

### Infrastructure
- [x] Project structure created
- [x] Documentation complete
- [x] Docker Compose configured
- [x] Database schema designed

### Services (To Be Implemented)
- [ ] Service 1: Telemetry Ingestion
- [ ] Service 2: Event Processing
- [ ] Service 3: Driver Scoring

### Testing
- [ ] Unit tests
- [ ] Integration tests
- [ ] End-to-end tests

### Documentation
- [x] README.md
- [x] Service specifications
- [x] API documentation
- [x] Learning roadmap
- [x] Troubleshooting guide

---

## 🎉 What Makes This Project Unique

1. **Complete Educational Package**
   - Step-by-step learning path
   - Discussion questions built-in
   - Real-world patterns

2. **Production-Ready Patterns**
   - Not a toy example
   - Scalable architecture
   - Best practices

3. **Hands-On Learning**
   - Build from scratch
   - Test everything
   - See results immediately

4. **Comprehensive Documentation**
   - Every decision explained
   - Trade-offs discussed
   - Multiple learning styles supported

---

## 📝 License & Usage

This project is designed for **educational purposes**. Feel free to:
- Use it for teaching
- Modify for your curriculum
- Share with students
- Build upon for other projects

---

## 🙏 Acknowledgments

This project demonstrates patterns used by:
- Uber (ride tracking)
- Tesla (vehicle telemetry)
- Logistics companies (fleet management)
- IoT platforms (sensor data processing)

The architecture scales from learning to production!

---

**Ready to start? Head to [README.md](README.md) and begin the journey! 🚀**
