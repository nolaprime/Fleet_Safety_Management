# 🚚 Fleet Management System - Educational Project

## Welcome!

This project teaches you how to build **real-time data processing systems** using **Apache Kafka** and **Spring Boot microservices**. You'll create a complete fleet management system that processes truck telemetry data in real-time.

---

## 📖 Start Here: Documentation Guide

### **Step 1: Learn Kafka Basics** (Start here if new to Kafka)
📄 [`docs/01_KAFKA_INTRODUCTION.md`](docs/01_KAFKA_INTRODUCTION.md)

**What you'll learn:**
- What is Kafka and why use it?
- Core concepts: Topics, Producers, Consumers, Partitions, Brokers, Zookeeper
- How Kafka works with real-world examples
- Kafka vs traditional message queues

**Time required:** 2-3 hours  
**Prerequisites:** None - beginner friendly!

---

### **Step 2: Learn Spring Kafka** (After understanding Kafka basics)
📄 [`docs/02_SPRING_KAFKA_GUIDE.md`](docs/02_SPRING_KAFKA_GUIDE.md)

**What you'll learn:**
- How to integrate Kafka with Spring Boot
- Writing producers with `KafkaTemplate`
- Writing consumers with `@KafkaListener`
- JSON serialization and deserialization
- Complete working examples you can copy and modify

**Time required:** 3-4 hours  
**Prerequisites:** Basic Java and Spring Boot knowledge

---

### **Step 3: Understand the Project** (Before you start coding)
📄 [`docs/03_PROJECT_REQUIREMENTS.md`](docs/03_PROJECT_REQUIREMENTS.md)

**What you'll learn:**
- Complete system architecture
- Detailed requirements for all 3 microservices
- Data models and API specifications
- Database schema
- Driver scoring algorithm
- Evaluation criteria

**Time required:** 2-3 hours  
**Prerequisites:** Steps 1 & 2 completed

---

### **Step 4: Follow the Roadmap** (Your implementation guide)
📄 [`docs/LEARNING_ROADMAP.md`](docs/LEARNING_ROADMAP.md)

**What you'll find:**
- Week-by-week learning schedule
- Step-by-step implementation guide
- Testing checkpoints
- Success checklist
- Tips and common pitfalls

**Time required:** 4-5 weeks total project time  
**Prerequisites:** Steps 1, 2, & 3 completed

---

## 🎯 Project Overview

### What You'll Build

A **Fleet Management System** with three microservices:

```
Service 1: Telemetry Ingestion (Producer)
    ↓ publishes to Kafka
Service 2: Event Processing (Consumer + Producer)
    ↓ publishes violations to Kafka  
Service 3: Driver Scoring (Consumer + REST API)
```

### Key Technologies

- **Apache Kafka** - Distributed messaging
- **Spring Boot** - Microservices framework
- **Spring Kafka** - Kafka integration
- **PostgreSQL** - Data persistence
- **Docker** - Container orchestration
- **Maven** - Build tool

---

## 🚀 Quick Start

### Prerequisites

- **Java 17** ([Installation guide](docs/README_STUDENT.md#prerequisites))
- **Maven 3.8+**
- **Docker Desktop**
- **Git**

### 1. Clone Repository

```bash
git clone <repository-url>
cd FleetManagementSystem
```

### 2. Start Infrastructure

```bash
cd docker
docker-compose up -d
```

This starts:
- Kafka & Zookeeper
- PostgreSQL database
- Kafka UI (http://localhost:8080)
- pgAdmin (http://localhost:5050)

### 3. Verify Kafka is Running

Open http://localhost:8080 in your browser. You should see the Kafka UI dashboard.

### 4. Start Learning

Begin with [`docs/01_KAFKA_INTRODUCTION.md`](docs/01_KAFKA_INTRODUCTION.md)

---

## 📚 Complete Documentation Index

### Core Learning Materials (Read in order)
1. [`01_KAFKA_INTRODUCTION.md`](docs/01_KAFKA_INTRODUCTION.md) - Kafka fundamentals for beginners
2. [`02_SPRING_KAFKA_GUIDE.md`](docs/02_SPRING_KAFKA_GUIDE.md) - Spring Kafka with examples
3. [`03_PROJECT_REQUIREMENTS.md`](docs/03_PROJECT_REQUIREMENTS.md) - Complete project specifications
4. [`LEARNING_ROADMAP.md`](docs/LEARNING_ROADMAP.md) - Week-by-week implementation guide

### Reference Materials
- [`KAFKA_CHEAT_SHEET.md`](docs/KAFKA_CHEAT_SHEET.md) - Quick command reference
- [`API_DOCUMENTATION.md`](docs/API_DOCUMENTATION.md) - REST API specifications
- [`TESTING_GUIDE.md`](docs/TESTING_GUIDE.md) - Testing strategies
- [`TROUBLESHOOTING.md`](docs/TROUBLESHOOTING.md) - Common issues and solutions

### Setup Guides
- [`README_STUDENT.md`](docs/README_STUDENT.md) - Detailed setup instructions
- [`KAFKA_SETUP.md`](docs/KAFKA_SETUP.md) - Kafka installation guide
- [`SETUP_MAVEN.md`](docs/SETUP_MAVEN.md) - Maven setup guide

---

## 🏗️ System Architecture

```
┌─────────────┐
│   Trucks    │ (Send telemetry data)
└──────┬──────┘
       │ HTTP POST
       ▼
┌────────────────────────────────────────┐
│  Service 1: Telemetry Ingestion       │
│  - REST API endpoint                   │
│  - Validates data                      │
│  - Publishes to Kafka                  │
└──────────────┬─────────────────────────┘
               │
               ▼ Kafka Topic: "raw-telemetry"
               │
       ┌───────┴───────┐
       ▼               ▼
┌──────────────┐  ┌──────────────────────┐
│  Service 2:  │  │   Service 3:         │
│  Event       │  │   Driver Scoring     │
│  Processing  │  │   - Stores in DB     │
│  - Detects   │  │   - Calculates scores│
│  violations  │  │   - REST API         │
│  - Enriches  │  └──────────────────────┘
│  data        │
└──────┬───────┘
       │
       ▼ Kafka Topic: "violations"
       │
       ▼
    (Consumed by Service 3)
```

---

## 🎓 Learning Objectives

By completing this project, you will:

✅ **Understand Kafka fundamentals**
- Topics, partitions, producers, consumers
- Consumer groups and offset management
- Message retention and replay

✅ **Master Spring Kafka**
- Configure producers and consumers
- Implement message serialization/deserialization
- Handle errors and failures

✅ **Build microservices architecture**
- Service-to-service communication via Kafka
- Decoupled, scalable design
- Event-driven architecture

✅ **Work with real-world patterns**
- Data ingestion pipelines
- Event processing and enrichment
- Real-time analytics and scoring

✅ **Apply best practices**
- Validation and error handling
- Logging and monitoring
- Testing strategies

---

## 📋 Project Requirements Summary

### Service 1: Telemetry Ingestion
- **Type:** Producer
- **Port:** 8081
- **Purpose:** Receive telemetry via REST API, publish to Kafka
- **Key endpoints:** `POST /api/telemetry/ingest`

### Service 2: Event Processing
- **Type:** Consumer + Producer
- **Port:** None (consumer only)
- **Purpose:** Detect violations, enrich data, publish events
- **Violations detected:** Speeding, low fuel, high temp, low tire pressure

### Service 3: Driver Scoring
- **Type:** Consumer + REST API
- **Port:** 8082
- **Purpose:** Store violations, calculate driver scores, provide analytics
- **Key endpoints:** 
  - `GET /api/drivers/{driverId}/score`
  - `GET /api/drivers/{driverId}/violations`
  - `GET /api/drivers/leaderboard`

---

## 🧪 Testing Your System

### 1. Send Test Telemetry

```bash
curl -X POST http://localhost:8081/api/telemetry/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "truckId": "TRUCK-001",
    "driverId": "DRV-12345",
    "speed": 105.5,
    "fuelLevel": 68.3,
    "engineTemp": 92.0,
    "location": {"latitude": 34.0522, "longitude": -118.2437},
    "tirePressure": {"frontLeft": 32.5, "frontRight": 32.3, "rearLeft": 80.0, "rearRight": 79.5}
  }'
```

### 2. Check Kafka UI

Visit http://localhost:8080 to see messages in topics:
- `raw-telemetry` - All telemetry messages
- `violations` - Detected violations

### 3. Query Driver Score

```bash
curl http://localhost:8082/api/drivers/DRV-12345/score
```

---

## 🛠️ Development Workflow

### Build All Services

```bash
mvn clean package -DskipTests
```

### Run Individual Service

```bash
cd telemetry-ingestion-service
mvn spring-boot:run
```

### View Logs

```bash
docker-compose logs -f telemetry-ingestion-service
docker-compose logs -f event-processing-service
docker-compose logs -f driver-scoring-service
```

### Stop Everything

```bash
docker-compose down
```

### Reset Everything (Fresh Start)

```bash
docker-compose down -v  # Removes all data
docker-compose up -d    # Restart fresh
```

---

## 📊 Monitoring Tools

- **Kafka UI:** http://localhost:8080
  - View topics, messages, consumer groups
  - Monitor lag and throughput

- **pgAdmin:** http://localhost:5050
  - Username: `admin@fleet.com`
  - Password: `admin`
  - View database tables and data

---

## ⏱️ Time Estimates

| Phase | Activity | Time |
|-------|----------|------|
| Week 1 | Read documentation (01-03) | 6-8 hours |
| Week 1 | Setup environment | 2-3 hours |
| Week 2 | Build Services 1 & 2 | 10-14 hours |
| Week 3 | Build Service 3 | 10-12 hours |
| Week 4 | Testing & refinement | 8-10 hours |
| Week 5 | Documentation & polish | 4-6 hours |
| **Total** | | **40-53 hours** |

---

## 🎯 Success Criteria

Your project is complete when:

- [ ] All three services run without errors
- [ ] Telemetry data flows through entire pipeline
- [ ] Violations are detected correctly
- [ ] Driver scores are calculated accurately
- [ ] All REST API endpoints work
- [ ] Database stores data properly
- [ ] You have unit tests for core logic
- [ ] Documentation is complete

---

## 💡 Tips for Success

1. **Follow the order:** Complete docs 01 → 02 → 03 before coding
2. **Test incrementally:** Test each service before moving to the next
3. **Use the tools:** Kafka UI and pgAdmin are invaluable for debugging
4. **Read error messages:** Kafka errors are usually clear about what's wrong
5. **Log everything:** You'll thank yourself when debugging
6. **Ask questions:** Use the troubleshooting guide or ask for help
7. **Take breaks:** This is a marathon, not a sprint

---

## 🆘 Getting Help

1. **Check documentation first:**
   - [`TROUBLESHOOTING.md`](docs/TROUBLESHOOTING.md) for common issues
   - [`KAFKA_CHEAT_SHEET.md`](docs/KAFKA_CHEAT_SHEET.md) for commands

2. **Use Kafka UI:** http://localhost:8080
   - Verify messages are being sent/received
   - Check consumer group lag

3. **Check logs:**
   ```bash
   docker-compose logs kafka
   docker-compose logs <service-name>
   ```

4. **Ask your instructor** with:
   - What you're trying to do
   - What's happening instead
   - Relevant error messages
   - What you've tried so far

---

## 🎓 What's Next?

After completing this project, consider:

1. **Add advanced features:**
   - Real-time dashboard with WebSockets
   - Email/SMS alerts for critical violations
   - Grafana monitoring dashboards
   - Data analytics and reporting

2. **Explore related technologies:**
   - Kafka Streams for stream processing
   - Kafka Connect for data integration
   - Schema Registry for data governance
   - Kubernetes for orchestration

3. **Build your own project:**
   - Apply these patterns to a different domain
   - Add your own creative features
   - Share your work on GitHub

---

## 📄 License

This project is for educational purposes.

---

## 🙏 Acknowledgments

This project is designed to teach real-world event-driven architecture patterns used at companies like Uber, Netflix, LinkedIn, and many others.

---

**Ready to begin?** Start with [`docs/01_KAFKA_INTRODUCTION.md`](docs/01_KAFKA_INTRODUCTION.md) 🚀
