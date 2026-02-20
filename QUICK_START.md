# 🚀 Fleet Management System - Quick Start

## 30-Second Overview
Three microservices demonstrate Kafka: **Telemetry Ingestion** receives truck data via REST and publishes to Kafka, **Event Processing** detects violations and republishes events, **Driver Scoring** calculates and stores driver performance scores.

---

## 🏃 Quick Start - Docker (Recommended)

**This is the primary way to run the entire system:**

```bash
# Build and run everything with Docker
./build-and-run-docker.sh
```

This single script will:
1. ✅ Build Maven artifacts (JARs)
2. ✅ Build Docker images for both services
3. ✅ Start all infrastructure (Kafka, PostgreSQL, UIs)
4. ✅ Start both microservices
5. ✅ Create Kafka topics automatically
6. ✅ Display service URLs and test commands

**Services will be available at:**
- Telemetry Ingestion API: http://localhost:8081
- Kafka UI: http://localhost:8080
- pgAdmin: http://localhost:5050

---

## 🧪 Testing the System

**Use the automated test script:**

```bash
./test-kafka-demo.sh
```

This sends 5 test messages (some with violations) and shows you what to expect in the logs.

**Or test manually:**

```bash
# Normal telemetry
curl -X POST http://localhost:8081/api/telemetry/ingest \
  -H "Content-Type: application/json" \
  -d '{"truckId": "TRUCK-001", "speed": 75.5}'

# Trigger speeding violation (speed > 80)
curl -X POST http://localhost:8081/api/telemetry/ingest \
  -H "Content-Type: application/json" \
  -d '{"truckId": "TRUCK-002", "speed": 95.0}'
```

✅ **Success**: Check logs to see violation detection!

---

## 📚 Documentation Guide (Learning Path)

| Document | Purpose | When to Use |
|----------|---------|-------------|
| **docs/00_START_HERE.md** | Master guide with project overview | Start here! |
| **docs/01_KAFKA_INTRODUCTION.md** | Kafka fundamentals for beginners | Learn Kafka basics first |
| **docs/02_SPRING_KAFKA_GUIDE.md** | Spring Kafka with code examples | Learn integration |
| **docs/03_PROJECT_REQUIREMENTS.md** | Complete project specifications | Before implementing |
| **docs/LEARNING_ROADMAP.md** | Week-by-week implementation guide | During development |
| **docs/KAFKA_CHEAT_SHEET.md** | Command reference | Quick lookup |
| **docs/TESTING_GUIDE.md** | Testing strategies | When testing |
| **docs/TROUBLESHOOTING.md** | Common issues and solutions | When stuck |

---

## 🔍 Key Files to Explore

### Service 1: Telemetry Ingestion (Producer)
- `telemetry-ingestion-service/src/main/java/com/fleet/telemetry/controller/TelemetryController.java` - REST endpoint
- `telemetry-ingestion-service/src/main/java/com/fleet/telemetry/service/TelemetryProducer.java` - Kafka producer
- `telemetry-ingestion-service/src/main/java/com/fleet/telemetry/config/KafkaProducerConfig.java` - Producer config

### Service 2: Event Processing (Consumer + Producer)
- `event-processing-service/src/main/java/com/fleet/processor/service/TelemetryConsumer.java` - Kafka consumer with violation detection
- `event-processing-service/src/main/java/com/fleet/processor/config/KafkaConsumerConfig.java` - Consumer config

---

## 📊 Monitoring

**View logs to see messages flowing:**

```bash
# All services
cd docker && docker-compose logs -f

# Specific service
docker-compose logs -f telemetry-ingestion-service
docker-compose logs -f event-processing-service
```

**View messages in Kafka UI:**
- Open http://localhost:8080
- Click on Topics → `raw-telemetry` to see incoming telemetry
- Check consumer groups to see processing status

---

## 🛑 Stopping the System

```bash
cd docker

# Stop all services
docker-compose down

# Stop and remove all data (fresh start)
docker-compose down -v
```

## 🔄 Reset and Re-Seed the Database

Use this when you want the sample data loaded again:

```bash
cd docker

# Remove the existing database volume
docker-compose down -v

# Start Postgres to re-run init scripts (schema + seed data)
docker-compose up -d postgres
```

After Postgres is healthy, you can start the rest of the stack:

```bash
docker-compose up -d
```

---

## 🎯 What You'll Learn

✅ **Kafka Producer** - Publishing messages to topics  
✅ **Kafka Consumer** - Subscribing and processing messages  
✅ **Topics & Partitions** - Message organization  
✅ **JSON Serialization** - Data conversion  
✅ **Event-Driven Architecture** - Asynchronous microservices  
✅ **Docker Deployment** - Containerized applications  

---

## 🔧 Useful URLs

- **Kafka UI**: http://localhost:8080 (view topics, messages, consumer groups)
- **Telemetry API**: http://localhost:8081 (send telemetry data)
- **pgAdmin**: http://localhost:5050 (database viewer - admin@fleet.com / admin)

---

## 🛠️ Alternative: Run Without Docker

If you prefer running services locally (for development/debugging):

```bash
# 1. Start infrastructure only
cd docker && docker-compose up -d zookeeper kafka postgres

# 2. Build services
mvn clean package -DskipTests

# 3. Run services in separate terminals
cd telemetry-ingestion-service && mvn spring-boot:run
cd event-processing-service && mvn spring-boot:run
```

**Note:** Use `./build-and-run-docker.sh` for production-like deployment.

---

## � Common Tasks

```bash
# Check Kafka status
docker-compose ps

# View Kafka logs
docker-compose logs -f kafka

# List topics
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092

# Check consumer group
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group event-processing-group \
  --describe

# Stop everything
docker-compose down
```

---

## 💡 Simple Experiments

1. **Stop consumer → send messages → restart consumer** (messages are replayed!)
2. **Send speed > 80** (triggers violation warning)
3. **Send 10 messages in a row** (watch sequential processing)

---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| Connection refused | Wait 30-60s for Kafka to start |
| Port 8081 in use | `lsof -i :8081` then `kill -9 <PID>` |
| Consumer not receiving | Check Kafka is running: `docker-compose ps` |
| Build fails | Verify Java 17+: `java -version` |

---

## 🎓 Learning Path

**Week 1**: Understand basic flow (producer → Kafka → consumer)  
**Week 2**: Modify code, add fields, change logic  
**Week 3**: Add second topic, multiple consumers  
**Week 4**: Add database, advanced patterns  

---

## ✅ You've Succeeded When You Can...

- [ ] Start all services without errors
- [ ] Send a message and see it in consumer logs
- [ ] Explain what producer/consumer/topic means
- [ ] View messages in Kafka UI
- [ ] Trigger a speed violation warning
- [ ] Stop consumer and replay messages

---

## 📦 What's Included

✅ REST API producer (port 8081)  
✅ Kafka consumer with `@KafkaListener`  
✅ Simple telemetry model (3 fields)  
✅ Speed violation detection (> 80 km/h)  
✅ Extensive documentation  
✅ Test script  
✅ Docker Compose for Kafka  

---

## 🚀 Ready to Start?

```bash
# Open README_STUDENT.md for detailed guide
cat README_STUDENT.md

# Or jump right in
./test-kafka-demo.sh
```

**Happy Learning!** 🎉

---

_For questions, check the documentation files in the `docs/` folder._
