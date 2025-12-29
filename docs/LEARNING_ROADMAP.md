# Fleet Management System - Complete Learning Roadmap# Learning Roadmap - Fleet Management System



## 📚 Learning Path Overview## 🎓 Educational Journey: Building Event-Driven Microservices



This project will teach you how to build a **real-time data processing system** using Apache Kafka and Spring Boot microservices. Follow this roadmap step by step.This roadmap guides your student through building the Fleet Management System from scratch, learning key concepts progressively.



------



## Phase 1: Understanding Kafka (Week 1)## Phase 1: Foundations (Week 1)



### Step 1: Learn Kafka Fundamentals### Day 1-2: Environment Setup

📖 **Read:** `01_KAFKA_INTRODUCTION.md`**Goal:** Get all tools installed and running



**What you'll learn:****Tasks:**

- What is Apache Kafka and why use it?- [ ] Install Java 17, Maven, Docker Desktop

- Core concepts: Topics, Producers, Consumers, Partitions- [ ] Follow [KAFKA_SETUP.md](KAFKA_SETUP.md) to install Kafka

- How Kafka works internally- [ ] Start Kafka and create test topic

- Real-world use cases- [ ] Test Kafka with console producer/consumer

- Kafka vs traditional message queues- [ ] Create PostgreSQL database



**Time:** 2-3 hours**Learning Checkpoints:**

- Understand what Kafka is and why we use it

**Checkpoint:** Can you explain to someone how a producer, Kafka, and consumer work together?- Know the difference between topics, partitions, and consumer groups

- Can send and receive messages using command line

---

**Exercises:**

### Step 2: Learn Spring Kafka Integration```bash

📖 **Read:** `02_SPRING_KAFKA_GUIDE.md`# Exercise 1: Create a topic

kafka-topics --create --topic test --bootstrap-server localhost:9092 --partitions 1

**What you'll learn:**

- How to configure Kafka in Spring Boot# Exercise 2: Send messages

- Writing producers with KafkaTemplatekafka-console-producer --topic test --bootstrap-server localhost:9092

- Writing consumers with @KafkaListener> Hello Kafka

- JSON serialization/deserialization> Message 2

- Error handling patterns

- Complete working examples# Exercise 3: Consume messages

kafka-console-consumer --topic test --from-beginning --bootstrap-server localhost:9092

**Time:** 3-4 hours```



**Hands-on Exercise:**---

1. Follow the Temperature Monitoring example in the guide

2. Create a simple producer that sends messages### Day 3-5: Understanding Spring Boot & Kafka Basics

3. Create a consumer that reads and logs messages**Goal:** Build a simple producer and consumer

4. Test with different data types (String, JSON objects)

**Tasks:**

**Checkpoint:** Can you send and receive JSON messages between two Spring Boot applications?- [ ] Create a basic Spring Boot project

- [ ] Add spring-kafka dependency

---- [ ] Write a simple Kafka producer that sends strings

- [ ] Write a simple Kafka consumer that prints messages

### Step 3: Set Up Your Development Environment- [ ] Test the flow end-to-end

📖 **Read:** `README_STUDENT.md`

**Key Concepts:**

**Tasks:**- Spring Boot auto-configuration

1. Install Java 17- KafkaTemplate for producing

2. Install Maven- @KafkaListener for consuming

3. Install Docker Desktop- Serialization/Deserialization

4. Clone this repository

5. Start Kafka and PostgreSQL:**Mini Project:**

   ```bash```java

   cd docker// Simple message producer

   docker-compose up -d@Service

   ```public class SimpleProducer {

6. Verify Kafka is running: http://localhost:8080 (Kafka UI)    @Autowired

    private KafkaTemplate<String, String> kafkaTemplate;

**Time:** 1-2 hours    

    public void sendMessage(String message) {

**Checkpoint:** Can you access Kafka UI and see the Kafka broker running?        kafkaTemplate.send("test-topic", message);

    }

---}



## Phase 2: Understanding the Project (Week 1-2)// Simple message consumer

@Service

### Step 4: Read Project Requirementspublic class SimpleConsumer {

📖 **Read:** `03_PROJECT_REQUIREMENTS.md`    @KafkaListener(topics = "test-topic", groupId = "test-group")

    public void consume(String message) {

**What you'll learn:**        System.out.println("Received: " + message);

- Complete system architecture    }

- Each service's responsibilities}

- Data models and validation rules```

- API specifications

- Database schema**Quiz:**

- Scoring algorithm1. What happens if you restart the consumer?

2. What is a consumer group?

**Time:** 2-3 hours3. How does Kafka ensure message ordering?



**Exercise:**---

1. Draw the system architecture on paper

2. List the three services and their roles## Phase 2: Service 1 - Telemetry Ingestion (Week 2)

3. Write down the violation detection rules

4. Understand the driver scoring algorithm### Day 1-2: Building the REST API

**Goal:** Accept telemetry data via HTTP

**Checkpoint:** Can you explain the complete data flow from truck to dashboard?

**Tasks:**

---- [ ] Review [SERVICE_1_TELEMETRY_INGESTION.md](SERVICE_1_TELEMETRY_INGESTION.md)

- [ ] Create TelemetryPayload model with validation annotations

## Phase 3: Building the System (Week 2-4)- [ ] Create TelemetryController with POST endpoint

- [ ] Test with curl/Postman

### Step 5: Build Service 1 - Telemetry Ingestion

📖 **Reference:** `02_SPRING_KAFKA_GUIDE.md` (Producer section)**Key Concepts:**

- REST API design

**Tasks:**- Jakarta Validation (@Valid, @NotNull, etc.)

1. Create Spring Boot project with dependencies- Request/Response DTOs

2. Create TelemetryData model class- HTTP status codes (202 Accepted)

3. Implement REST controller for /api/telemetry/ingest

4. Add validation logic (Bean Validation)**Exercises:**

5. Configure Kafka producer- What happens if you send invalid latitude (> 90)?

6. Implement KafkaTemplate producer- What HTTP status code should we return for success?

7. Add proper logging- Why 202 Accepted instead of 200 OK?

8. Test with curl or Postman

---

**Time:** 4-6 hours

### Day 3-4: Kafka Producer Integration

**Testing:****Goal:** Publish validated telemetry to Kafka

```bash

curl -X POST http://localhost:8081/api/telemetry/ingest \**Tasks:**

  -H "Content-Type: application/json" \- [ ] Create KafkaProducerService

  -d '{- [ ] Serialize TelemetryPayload to JSON

    "truckId": "TRUCK-001",- [ ] Use device_id as message key (for partitioning)

    "driverId": "DRV-12345",- [ ] Implement fire-and-forget pattern

    "speed": 75.5,- [ ] Add async callback for logging

    "fuelLevel": 68.3,

    "engineTemp": 92.0**Key Concepts:**

  }'- Fire-and-forget vs synchronous sends

```- Message keys and partitioning

- CompletableFuture for async operations

**Checkpoint:** - Error handling in async code

- Can you send telemetry data and see it in Kafka UI?

- Does validation work correctly?**Challenge:**

- Are messages appearing in the "raw-telemetry" topic?```java

// Implement this method

---public void sendTelemetry(TelemetryPayload payload) {

    // 1. Convert payload to JSON

### Step 6: Build Service 2 - Event Processing    // 2. Send to Kafka with device_id as key

📖 **Reference:** `02_SPRING_KAFKA_GUIDE.md` (Consumer section)    // 3. Add callback to log success/failure

}

**Tasks:**```

1. Create Spring Boot project

2. Create TelemetryData and ViolationEvent models---

3. Configure Kafka consumer for "raw-telemetry" topic

4. Implement @KafkaListener consumer### Day 5: Testing

5. Implement violation detection logic:**Goal:** Write comprehensive tests

   - Speeding detection

   - Low fuel detection**Tasks:**

   - High engine temperature detection- [ ] Unit test for validation

   - Low tire pressure detection- [ ] Integration test with @EmbeddedKafka

6. Configure Kafka producer for "violations" topic- [ ] Test error scenarios

7. Publish ViolationEvent when violations detected

8. Add comprehensive logging**Key Concepts:**

- @SpringBootTest

**Time:** 6-8 hours- @EmbeddedKafka

- MockMvc for API testing

**Testing:**- Test assertions

1. Start Service 1 and Service 2

2. Send test telemetry with violations:---

   ```bash

   # Speeding violation## Phase 3: Service 2 - Event Processing (Week 3)

   curl -X POST http://localhost:8081/api/telemetry/ingest \

     -H "Content-Type: application/json" \### Day 1-2: Kafka Consumer & Normalization

     -d '{"truckId": "TRUCK-001", "driverId": "DRV-12345", "speed": 105.5, ...}'**Goal:** Consume raw telemetry and clean the data

   

   # Low fuel violation**Tasks:**

   curl -X POST http://localhost:8081/api/telemetry/ingest \- [ ] Review [SERVICE_2_EVENT_PROCESSING.md](SERVICE_2_EVENT_PROCESSING.md)

     -H "Content-Type: application/json" \- [ ] Create TelemetryConsumer with @KafkaListener

     -d '{"truckId": "TRUCK-002", "driverId": "DRV-67890", "fuelLevel": 3.0, ...}'- [ ] Implement NormalizationService

   ```- [ ] Publish to normalized_telemetry topic

3. Check Service 2 logs for violation detection- [ ] Test with console consumer

4. Verify violations in Kafka UI ("violations" topic)

**Key Concepts:**

**Checkpoint:**- Consumer group management

- Does Service 2 consume all telemetry messages?- Offset management

- Are violations detected correctly?- Batch processing (concurrency)

- Are violation events published to Kafka?- Data transformation pipelines



---**Exercises:**

- What does `auto-offset-reset: earliest` mean?

### Step 7: Build Service 3 - Driver Scoring- What happens if processing fails?

📖 **Reference:** `03_PROJECT_REQUIREMENTS.md` (Service 3 section)- How do we handle poison messages?



**Tasks:**---

1. Create Spring Boot project with Spring Data JPA

2. Set up PostgreSQL connection### Day 3-5: Business Rules Implementation

3. Create database entities:**Goal:** Detect violations and publish events

   - Violation entity

   - DriverScore entity**Tasks:**

4. Create JPA repositories- [ ] Implement SpeedingRule

5. Configure Kafka consumer for "violations" topic- [ ] Implement HarshBrakingRule

6. Implement violation storage in database- [ ] Implement FatigueRule (requires state!)

7. Implement driver score calculation algorithm- [ ] Implement GeofenceRule

8. Create REST API endpoints:- [ ] Create RuleEngine to orchestrate all rules

   - GET /api/drivers/{driverId}/score- [ ] Publish DrivingEvents to driving_events topic

   - GET /api/drivers/{driverId}/violations

   - GET /api/drivers/leaderboard**Key Concepts:**

   - GET /api/drivers/bottom- Strategy pattern for rules

9. Test all endpoints- Stateless vs stateful processing

- Split-stream pattern (one input → two outputs)

**Time:** 8-10 hours- In-memory state management



**Testing:****Critical Thinking:**

1. Start all three services```

2. Send multiple telemetry messages with violationsWhy do we need two output topics?

3. Wait for processing- normalized_telemetry: For trip calculation (clean data)

4. Query driver score:- driving_events: For violations (rule violations)

   ```bash

   curl http://localhost:8082/api/drivers/DRV-12345/scoreWhy not combine them?

   ```- Different consumers need different data

5. Check database using pgAdmin (http://localhost:5050)- Enables parallel processing

- Separation of concerns

**Checkpoint:**```

- Are violations stored in the database?

- Are driver scores calculated correctly?---

- Do API endpoints return proper JSON responses?

### Day 6-7: Advanced Rule - Fatigue Detection

---**Goal:** Implement stateful processing



## Phase 4: Testing and Refinement (Week 4-5)**Tasks:**

- [ ] Create DriverStateManager

### Step 8: End-to-End Testing- [ ] Track continuous driving time per driver

📖 **Read:** `TESTING_GUIDE.md`- [ ] Reset state when ignition turns OFF

- [ ] Detect fatigue after 4 hours of continuous driving

**Tasks:**

1. Test complete flow with various scenarios**Key Concepts:**

2. Test edge cases (invalid data, extreme values)- Stateful stream processing

3. Test error handling (Kafka down, database down)- ConcurrentHashMap for thread safety

4. Performance testing (send 100 messages rapidly)- State expiration and cleanup

5. Fix any bugs discovered- Time-window aggregations



**Time:** 4-6 hours**Challenge:**

```java

**Test Scenarios:**// How do you track driving time across multiple messages?

- Normal telemetry (no violations)// Hint: Keep a map of driver_id -> DriverState

- Single violation```

- Multiple violations simultaneously

- Invalid data (should be rejected)---

- Missing required fields

- Out-of-range values## Phase 4: Service 3 - Driver Scoring (Week 4)

- Driver with no violations (score = 100)

- Driver with many violations (low score)### Day 1-3: Trip State Machine

**Goal:** Track trip lifecycle and calculate distance

---

**Tasks:**

### Step 9: Add Unit Tests- [ ] Review [SERVICE_3_DRIVER_SCORING.md](SERVICE_3_DRIVER_SCORING.md)

- [ ] Design trip state machine (NO_TRIP → TRIP_ACTIVE → TRIP_COMPLETED)

**Tasks:**- [ ] Implement Haversine formula for GPS distance

1. Write unit tests for violation detection logic- [ ] Handle ignition ON (start trip)

2. Write unit tests for score calculation- [ ] Handle ignition OFF (end trip, persist to DB)

3. Write unit tests for validation logic- [ ] Calculate total miles per trip

4. Mock Kafka producers/consumers for testing

5. Aim for >70% code coverage**Key Concepts:**

- State machines

**Time:** 6-8 hours- Event sourcing (rebuilding state from events)

- Geospatial calculations

---- Stateful aggregations



### Step 10: Documentation**Math Challenge:**

```

**Tasks:**Implement Haversine formula to calculate distance between two GPS points:

1. Write README for each service with:Point A: (34.0522°N, 118.2437°W)

   - Purpose and responsibilitiesPoint B: (34.0622°N, 118.2437°W)

   - How to build and run

   - API documentationExpected: ~0.7 miles

   - Configuration options```

2. Update main README

3. Add code comments for complex logic---

4. Document any assumptions or limitations

### Day 4-5: Database Integration

**Time:** 2-3 hours**Goal:** Persist trips and violations



---**Tasks:**

- [ ] Set up PostgreSQL schema (see init-db.sql)

## Phase 5: Advanced Features (Optional, Week 5-6)- [ ] Create JPA entities (Driver, Trip, Violation)

- [ ] Create repositories

### Optional Enhancements- [ ] Implement save operations

- [ ] Add database triggers for auto-updating driver stats

1. **Add Metrics and Monitoring**

   - Spring Boot Actuator**Key Concepts:**

   - Prometheus metrics- JPA/Hibernate basics

   - Grafana dashboards- Entity relationships (@OneToMany)

- Repository pattern

2. **Add Alert Notifications**- Transactions

   - Email alerts for critical violations

   - Slack integration---

   - SMS alerts

### Day 6-7: Scoring Engine & REST API

3. **Add Caching****Goal:** Calculate scores and expose query APIs

   - Redis cache for driver scores

   - Improve API response times**Tasks:**

- [ ] Implement ScoringEngine

4. **Add Real-time Dashboard**- [ ] Define point deduction rules

   - WebSocket for live updates- [ ] Update driver score on violation

   - React/Angular frontend- [ ] Create REST endpoints (GET /driver/{id}/score, /trips, /violations)

   - Real-time violation map- [ ] Add pagination

- [ ] Test with curl

5. **Add Data Replay**

   - Ability to reprocess historical data**Key Concepts:**

   - Reset consumer offsets- CQRS pattern (Command Query Responsibility Segregation)

   - Batch processing mode- Write side: Kafka consumers updating state

- Read side: REST API querying database

6. **Add Authentication**- Pagination and filtering

   - JWT tokens

   - Role-based access control---

   - API key management

## Phase 5: Integration & Testing (Week 5)

---

### Day 1-3: End-to-End Testing

## Resources by Topic**Goal:** Verify complete data flow



### Kafka Fundamentals**Tasks:**

- `01_KAFKA_INTRODUCTION.md` - Complete beginner's guide- [ ] Create end-to-end test script

- `KAFKA_CHEAT_SHEET.md` - Quick reference- [ ] Simulate complete trip lifecycle

- Official Kafka Docs: https://kafka.apache.org/documentation/- [ ] Trigger multiple violations

- [ ] Verify score updates

### Spring Kafka- [ ] Check database state

- `02_SPRING_KAFKA_GUIDE.md` - Complete examples

- Spring Kafka Docs: https://spring.io/projects/spring-kafka**Scenarios to Test:**

1. Normal trip (no violations)

### Project Requirements2. Trip with speeding violation

- `03_PROJECT_REQUIREMENTS.md` - Complete specifications3. Trip with harsh braking

- `API_DOCUMENTATION.md` - API reference4. Trip with fatigue detection

5. Multiple concurrent trips

### Troubleshooting

- `TROUBLESHOOTING.md` - Common issues and solutions---

- `TESTING_GUIDE.md` - Testing strategies

### Day 4-5: Monitoring & Debugging

---**Goal:** Learn to troubleshoot issues



## Weekly Schedule Suggestion**Tasks:**

- [ ] Use Kafka UI to monitor topics

### Week 1: Learning- [ ] Check consumer lag

- **Day 1-2:** Read Kafka Introduction, understand concepts- [ ] Query database for debugging

- **Day 3-4:** Read Spring Kafka Guide, try examples- [ ] Add custom logging

- **Day 5:** Set up environment, start Kafka- [ ] Use Spring Boot Actuator

- **Day 6-7:** Read project requirements, plan architecture

**Tools:**

### Week 2: Service 1 & 2- Kafka UI: http://localhost:8080

- **Day 1-2:** Build Telemetry Ingestion Service- pgAdmin: http://localhost:5050

- **Day 3:** Test Service 1 thoroughly- Actuator: http://localhost:8083/actuator/health

- **Day 4-6:** Build Event Processing Service

- **Day 7:** Test Services 1 & 2 together---



### Week 3: Service 3## Phase 6: Advanced Topics (Week 6)

- **Day 1-3:** Build Driver Scoring Service (database + Kafka consumer)

- **Day 4-5:** Build REST APIs### Enhancements to Implement

- **Day 6-7:** End-to-end testing

**Enhancement 1: Dead Letter Queue**

### Week 4: Testing & Polish- Handle failed message processing

- **Day 1-3:** Unit tests and integration tests- Route poison messages to error topic

- **Day 4-5:** Bug fixes and improvements- Implement retry logic

- **Day 6-7:** Documentation and final testing

**Enhancement 2: Exactly-Once Semantics**

### Week 5: Advanced (Optional)- Enable Kafka idempotent producer

- Add optional enhancements based on interest- Implement transactional consumers

- Handle duplicates

---

**Enhancement 3: Real-Time Dashboard**

## Success Checklist- Create WebSocket endpoint

- Push violations to frontend in real-time

### Kafka Understanding ✅- Build simple React/Vue.js dashboard

- [ ] Can explain what Kafka is and why it's useful

- [ ] Understand topics, producers, and consumers**Enhancement 4: External Speed Limit API**

- [ ] Know how partitions work- Integrate with OpenStreetMap Overpass API

- [ ] Understand consumer groups- Get real speed limits by GPS coordinates

- Cache results to avoid excessive API calls

### Spring Kafka ✅

- [ ] Can configure a Kafka producer in Spring Boot---

- [ ] Can configure a Kafka consumer in Spring Boot

- [ ] Can send and receive JSON messages## Assessment Milestones

- [ ] Understand error handling patterns

### Milestone 1: Service 1 Complete

### Project Implementation ✅**Criteria:**

- [ ] Service 1 receives and validates telemetry- [ ] REST API accepts valid telemetry

- [ ] Service 1 publishes to Kafka successfully- [ ] Messages published to Kafka

- [ ] Service 2 consumes telemetry messages- [ ] Unit tests pass

- [ ] Service 2 detects all violation types correctly- [ ] Can demonstrate with curl

- [ ] Service 2 publishes violation events

- [ ] Service 3 consumes violation events### Milestone 2: Service 2 Complete

- [ ] Service 3 stores violations in database**Criteria:**

- [ ] Service 3 calculates driver scores correctly- [ ] Consumes raw telemetry

- [ ] All REST API endpoints work- [ ] Normalizes data correctly

- [ ] Complete data flow works end-to-end- [ ] Detects at least 2 rule violations

- [ ] Publishes to both output topics

### Testing ✅- [ ] Tests pass

- [ ] Unit tests for business logic

- [ ] Integration tests for Kafka### Milestone 3: Service 3 Complete

- [ ] API endpoint tests**Criteria:**

- [ ] Edge case testing- [ ] Trip state machine works correctly

- [ ] Error handling tested- [ ] Distance calculation accurate

- [ ] Scores update on violations

### Documentation ✅- [ ] REST API returns correct data

- [ ] Each service has README- [ ] Database populated

- [ ] API documentation complete

- [ ] Setup instructions clear### Final Project: Complete System

- [ ] Code commented**Criteria:**

- [ ] All services running

---- [ ] End-to-end data flow works

- [ ] Can demonstrate complete trip lifecycle

## Tips for Success- [ ] Score calculation correct

- [ ] All tests passing

1. **Follow the order:** Don't skip ahead - each step builds on previous ones- [ ] Documentation updated

2. **Test incrementally:** Test each piece before moving on

3. **Use Kafka UI:** Invaluable for debugging message flow---

4. **Log everything:** Good logging makes debugging much easier

5. **Ask for help:** Use the troubleshooting guide and don't hesitate to ask questions## Learning Resources

6. **Take breaks:** This is a lot of content - pace yourself

7. **Experiment:** Try variations and see what happens### Kafka Fundamentals

8. **Document as you go:** Write down problems and solutions- [Kafka Documentation](https://kafka.apache.org/documentation/)

- [Spring Kafka Reference](https://docs.spring.io/spring-kafka/reference/)

---- Book: "Kafka: The Definitive Guide" by Neha Narkhede



## Common Pitfalls to Avoid### Event-Driven Architecture

- [Event Sourcing Pattern](https://martinfowler.com/eaaDev/EventSourcing.html)

1. ❌ Skipping Kafka basics - understand fundamentals first- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)

2. ❌ Not testing each service independently before integration- [Enterprise Integration Patterns](https://www.enterpriseintegrationpatterns.com/)

3. ❌ Ignoring error handling - production systems must handle failures

4. ❌ Poor logging - you'll regret it when debugging### Spring Boot

5. ❌ Not using Kafka UI to verify messages- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/)

6. ❌ Hardcoding values - use configuration properties- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)

7. ❌ Forgetting to commit consumer offsets properly- [Baeldung Tutorials](https://www.baeldung.com/)

8. ❌ Not handling JSON serialization/deserialization correctly

---

---

## Discussion Questions

## Next Steps

**Architecture:**

Start with: **`01_KAFKA_INTRODUCTION.md`**1. Why use Kafka instead of REST calls between services?

2. What are the trade-offs of event-driven vs request-response?

Good luck with your learning journey! 🚀3. How would you handle service failures?


**Scaling:**
1. How would you scale Service 1 to handle 10,000 requests/second?
2. What happens if Service 2 can't keep up with Service 1?
3. How do Kafka partitions help with scaling?

**Data Consistency:**
1. What if a violation is processed but database save fails?
2. How do we ensure exactly-once processing?
3. What is the CAP theorem and how does it apply here?

**Design Decisions:**
1. Why fire-and-forget in Service 1 instead of waiting for Kafka ack?
2. Why use two output topics instead of one?
3. Why keep trip state in memory vs always querying database?

---

## Project Completion Checklist

### Infrastructure
- [ ] Kafka running and topics created
- [ ] PostgreSQL running with schema
- [ ] All dependencies installed

### Services
- [ ] Service 1: Telemetry Ingestion working
- [ ] Service 2: Event Processing working
- [ ] Service 3: Driver Scoring working

### Testing
- [ ] Unit tests passing
- [ ] Integration tests passing
- [ ] End-to-end test script works

### Documentation
- [ ] README.md updated
- [ ] API documentation complete
- [ ] Code commented
- [ ] Architecture diagram drawn

### Demo Preparation
- [ ] Sample data prepared
- [ ] Demo script written
- [ ] Screenshots/recordings taken
- [ ] Presentation slides ready

---

## Next Steps After Completion

**Production Readiness:**
- Implement proper security (OAuth2, JWT)
- Add distributed tracing (Zipkin, Jaeger)
- Set up proper logging (ELK stack)
- Add monitoring (Prometheus, Grafana)
- Containerize with Docker
- Deploy to Kubernetes

**Additional Features:**
- Real-time dashboard with WebSocket
- Email/SMS alerts for critical violations
- Driver behavior analytics
- Predictive maintenance
- Route optimization

**Other Microservices Patterns:**
- API Gateway (Spring Cloud Gateway)
- Service Discovery (Eureka)
- Circuit Breaker (Resilience4j)
- Distributed Configuration (Spring Cloud Config)

---

## Success Criteria

By the end of this project, your student should be able to:

✅ Explain event-driven architecture  
✅ Build Kafka producers and consumers in Spring Boot  
✅ Implement business rules and data transformations  
✅ Design stateful stream processing applications  
✅ Work with state machines and event sourcing  
✅ Build REST APIs for querying aggregated data  
✅ Test microservices with embedded Kafka  
✅ Debug issues using Kafka UI and logs  
✅ Understand trade-offs in distributed systems  

**Congratulations on completing the Fleet Management System! 🎉**

This project demonstrates real-world skills that are highly valued in the industry. The patterns learned here apply to many domains: IoT, finance, e-commerce, and more.
