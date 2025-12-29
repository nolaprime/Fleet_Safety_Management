# Spring Kafka - Complete Guide with Examples

## Introduction

Spring Kafka provides a simple and powerful way to integrate Apache Kafka with Spring Boot applications. It handles the complex configuration and provides intuitive annotations for producing and consuming messages.

---

## Maven Dependencies

First, add Spring Kafka to your `pom.xml`:

```xml
<dependencies>
    <!-- Spring Boot Starter for Kafka -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    
    <!-- If you need JSON serialization -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
</dependencies>
```

---

## Basic Configuration

### application.properties

```properties
# Kafka Broker Address
spring.kafka.bootstrap-servers=localhost:9092

# Producer Configuration
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

# Consumer Configuration
spring.kafka.consumer.group-id=my-consumer-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
```

---

## Part 1: Writing to Kafka (Producer)

### Simple Example: Sending String Messages

#### Step 1: Create a Simple Producer

```java
package com.example.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SimpleProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    public SimpleProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
        System.out.println("Sent: " + message);
    }
}
```

#### Step 2: Use the Producer

```java
@RestController
@RequestMapping("/api")
public class MessageController {
    
    private final SimpleProducer producer;
    
    public MessageController(SimpleProducer producer) {
        this.producer = producer;
    }
    
    @PostMapping("/send")
    public String sendMessage(@RequestParam String message) {
        producer.sendMessage("test-topic", message);
        return "Message sent: " + message;
    }
}
```

#### Test It:
```bash
curl -X POST "http://localhost:8080/api/send?message=Hello Kafka"
```

---

### Advanced Example: Sending JSON Objects

#### Step 1: Create a Data Model

```java
package com.example.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String orderId;
    private String productName;
    private double price;
    private long timestamp;
}
```

#### Step 2: Configure the Producer

```java
package com.example.kafka.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        
        // Kafka broker address
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Serializers: Convert Java objects to bytes
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        return new DefaultKafkaProducerFactory<>(config);
    }
    
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

#### Step 3: Create a Producer Service

```java
package com.example.kafka.producer;

import com.example.kafka.model.OrderEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "order-events";
    
    public OrderProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * Send order event to Kafka
     * Fire-and-forget pattern: Don't wait for acknowledgment
     */
    public void sendOrder(OrderEvent order) {
        kafkaTemplate.send(TOPIC, order.getOrderId(), order);
        System.out.println("Order sent: " + order.getOrderId());
    }
    
    /**
     * Send order with callback - know if it succeeded or failed
     */
    public void sendOrderWithCallback(OrderEvent order) {
        kafkaTemplate.send(TOPIC, order.getOrderId(), order)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    System.out.println("Sent successfully: " + order.getOrderId() 
                        + " to partition " + result.getRecordMetadata().partition());
                } else {
                    System.err.println("Failed to send: " + ex.getMessage());
                }
            });
    }
}
```

#### Step 4: Use the Producer

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private final OrderProducer orderProducer;
    
    public OrderController(OrderProducer orderProducer) {
        this.orderProducer = orderProducer;
    }
    
    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderEvent order) {
        order.setTimestamp(System.currentTimeMillis());
        orderProducer.sendOrderWithCallback(order);
        return ResponseEntity.ok("Order sent: " + order.getOrderId());
    }
}
```

#### Test It:
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-001",
    "productName": "Laptop",
    "price": 999.99
  }'
```

---

## Part 2: Reading from Kafka (Consumer)

### Simple Example: Consuming String Messages

```java
package com.example.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SimpleConsumer {
    
    @KafkaListener(topics = "test-topic", groupId = "my-consumer-group")
    public void consume(String message) {
        System.out.println("Received: " + message);
        
        // Process the message here
        processMessage(message);
    }
    
    private void processMessage(String message) {
        // Your business logic
        System.out.println("Processing: " + message);
    }
}
```

**How it works:**
1. `@KafkaListener` annotation automatically polls Kafka for new messages
2. Spring calls `consume()` method for each message
3. `topics` - which topic(s) to listen to
4. `groupId` - consumer group for load balancing

---

### Advanced Example: Consuming JSON Objects

#### Step 1: Configure the Consumer

```java
package com.example.kafka.config;

import com.example.kafka.model.OrderEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public ConsumerFactory<String, OrderEvent> orderConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        
        // Kafka broker address
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Consumer group ID
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "order-consumer-group");
        
        // Start reading from the beginning if no offset exists
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // Deserializers: Convert bytes to Java objects
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Configure JSON deserializer
        JsonDeserializer<OrderEvent> deserializer = new JsonDeserializer<>(OrderEvent.class, false);
        deserializer.addTrustedPackages("com.example.kafka.model");
        
        return new DefaultKafkaConsumerFactory<>(
            config,
            new StringDeserializer(),
            deserializer
        );
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent> orderKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderConsumerFactory());
        return factory;
    }
}
```

#### Step 2: Create a Consumer Service

```java
package com.example.kafka.consumer;

import com.example.kafka.model.OrderEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {
    
    /**
     * Simple consumer - processes each order
     */
    @KafkaListener(
        topics = "order-events",
        groupId = "order-consumer-group",
        containerFactory = "orderKafkaListenerContainerFactory"
    )
    public void consumeOrder(OrderEvent order) {
        System.out.println("Received order: " + order.getOrderId());
        System.out.println("Product: " + order.getProductName());
        System.out.println("Price: $" + order.getPrice());
        
        // Process the order
        processOrder(order);
    }
    
    /**
     * Advanced consumer - receives message metadata
     */
    @KafkaListener(
        topics = "order-events",
        groupId = "order-analytics-group"
    )
    public void consumeOrderWithMetadata(
            OrderEvent order,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp) {
        
        System.out.println("Order: " + order.getOrderId());
        System.out.println("Partition: " + partition);
        System.out.println("Offset: " + offset);
        System.out.println("Timestamp: " + new Date(timestamp));
        
        // Analytics processing
        updateAnalytics(order);
    }
    
    private void processOrder(OrderEvent order) {
        // Your business logic
        // - Update inventory
        // - Process payment
        // - Send confirmation email
    }
    
    private void updateAnalytics(OrderEvent order) {
        // Analytics logic
        // - Update sales dashboard
        // - Calculate metrics
    }
}
```

---

## Consumer Patterns

### Pattern 1: Multiple Consumers in Same Group (Load Balancing)

```java
// Consumer 1
@Service
public class OrderProcessor1 {
    @KafkaListener(topics = "orders", groupId = "order-processors")
    public void process(OrderEvent order) {
        System.out.println("Processor 1 handling: " + order.getOrderId());
    }
}

// Consumer 2
@Service
public class OrderProcessor2 {
    @KafkaListener(topics = "orders", groupId = "order-processors")
    public void process(OrderEvent order) {
        System.out.println("Processor 2 handling: " + order.getOrderId());
    }
}
```

**Result:** Orders are distributed between processors for parallel processing.

---

### Pattern 2: Multiple Consumer Groups (Broadcast)

```java
// Inventory Service
@Service
public class InventoryService {
    @KafkaListener(topics = "orders", groupId = "inventory-group")
    public void updateInventory(OrderEvent order) {
        System.out.println("Inventory: Reserve " + order.getProductName());
    }
}

// Payment Service
@Service
public class PaymentService {
    @KafkaListener(topics = "orders", groupId = "payment-group")
    public void processPayment(OrderEvent order) {
        System.out.println("Payment: Charge $" + order.getPrice());
    }
}

// Notification Service
@Service
public class NotificationService {
    @KafkaListener(topics = "orders", groupId = "notification-group")
    public void sendEmail(OrderEvent order) {
        System.out.println("Email: Confirming " + order.getOrderId());
    }
}
```

**Result:** ALL three services receive EVERY order message!

---

## Error Handling

### Handling Consumer Errors

```java
@Service
public class ResilientConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(ResilientConsumer.class);
    
    @KafkaListener(topics = "orders", groupId = "order-group")
    public void consume(OrderEvent order) {
        try {
            // Process the order
            processOrder(order);
            
        } catch (BusinessException ex) {
            log.error("Business error for order {}: {}", order.getOrderId(), ex.getMessage());
            // Send to error topic or dead letter queue
            sendToErrorTopic(order, ex);
            
        } catch (Exception ex) {
            log.error("Unexpected error for order {}", order.getOrderId(), ex);
            // Rethrow to trigger retry
            throw ex;
        }
    }
    
    private void sendToErrorTopic(OrderEvent order, Exception ex) {
        // Send failed message to error topic for manual review
        kafkaTemplate.send("order-errors", order);
    }
}
```

---

## Common Configuration Properties

### Producer Properties

```properties
# Basic
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

# Performance
spring.kafka.producer.batch-size=16384
spring.kafka.producer.buffer-memory=33554432
spring.kafka.producer.compression-type=snappy

# Reliability
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3
```

### Consumer Properties

```properties
# Basic
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=my-group
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer

# Offset Management
spring.kafka.consumer.auto-offset-reset=earliest  # Options: earliest, latest, none
spring.kafka.consumer.enable-auto-commit=true

# JSON Deserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=com.example.kafka.model
```

---

## Complete Working Example

### Scenario: Real-Time Temperature Monitoring

#### Data Model
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemperatureReading {
    private String sensorId;
    private double temperature;
    private long timestamp;
    private String location;
}
```

#### Producer
```java
@Service
public class TemperatureSensor {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public TemperatureSensor(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void sendReading(String sensorId, double temperature, String location) {
        TemperatureReading reading = new TemperatureReading(
            sensorId,
            temperature,
            System.currentTimeMillis(),
            location
        );
        
        kafkaTemplate.send("temperature-readings", sensorId, reading);
        System.out.println("Sent: " + sensorId + " = " + temperature + "°C");
    }
}
```

#### Consumer - Alert Service
```java
@Service
public class TemperatureAlertService {
    
    private static final double HIGH_TEMP_THRESHOLD = 30.0;
    private static final double LOW_TEMP_THRESHOLD = 5.0;
    
    @KafkaListener(topics = "temperature-readings", groupId = "alert-service")
    public void checkTemperature(TemperatureReading reading) {
        if (reading.getTemperature() > HIGH_TEMP_THRESHOLD) {
            System.out.println("🔥 HIGH TEMPERATURE ALERT!");
            System.out.println("Sensor: " + reading.getSensorId());
            System.out.println("Location: " + reading.getLocation());
            System.out.println("Temperature: " + reading.getTemperature() + "°C");
            sendAlert(reading);
        } else if (reading.getTemperature() < LOW_TEMP_THRESHOLD) {
            System.out.println("❄️ LOW TEMPERATURE ALERT!");
            System.out.println("Sensor: " + reading.getSensorId());
            System.out.println("Location: " + reading.getLocation());
            System.out.println("Temperature: " + reading.getTemperature() + "°C");
            sendAlert(reading);
        }
    }
    
    private void sendAlert(TemperatureReading reading) {
        // Send email, SMS, or push notification
    }
}
```

#### Consumer - Analytics Service
```java
@Service
public class TemperatureAnalyticsService {
    
    private final Map<String, List<Double>> sensorHistory = new ConcurrentHashMap<>();
    
    @KafkaListener(topics = "temperature-readings", groupId = "analytics-service")
    public void analyze(TemperatureReading reading) {
        // Store reading
        sensorHistory
            .computeIfAbsent(reading.getSensorId(), k -> new ArrayList<>())
            .add(reading.getTemperature());
        
        // Calculate average
        List<Double> history = sensorHistory.get(reading.getSensorId());
        double average = history.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        System.out.println("Sensor " + reading.getSensorId() 
            + " - Current: " + reading.getTemperature() 
            + "°C, Average: " + String.format("%.2f", average) + "°C");
    }
}
```

#### REST Controller
```java
@RestController
@RequestMapping("/api/sensors")
public class SensorController {
    
    private final TemperatureSensor sensor;
    
    public SensorController(TemperatureSensor sensor) {
        this.sensor = sensor;
    }
    
    @PostMapping("/reading")
    public ResponseEntity<String> submitReading(
            @RequestParam String sensorId,
            @RequestParam double temperature,
            @RequestParam String location) {
        
        sensor.sendReading(sensorId, temperature, location);
        return ResponseEntity.ok("Reading submitted: " + temperature + "°C");
    }
}
```

#### Test It:
```bash
# Send normal reading
curl -X POST "http://localhost:8080/api/sensors/reading?sensorId=SENSOR-001&temperature=22.5&location=Warehouse-A"

# Send high temperature alert
curl -X POST "http://localhost:8080/api/sensors/reading?sensorId=SENSOR-002&temperature=35.0&location=Warehouse-B"

# Send low temperature alert
curl -X POST "http://localhost:8080/api/sensors/reading?sensorId=SENSOR-003&temperature=2.0&location=Cold-Storage"
```

**Output:**
```
Sent: SENSOR-001 = 22.5°C
Sensor SENSOR-001 - Current: 22.5°C, Average: 22.50°C

Sent: SENSOR-002 = 35.0°C
🔥 HIGH TEMPERATURE ALERT!
Sensor: SENSOR-002
Location: Warehouse-B
Temperature: 35.0°C

Sent: SENSOR-003 = 2.0°C
❄️ LOW TEMPERATURE ALERT!
Sensor: SENSOR-003
Location: Cold-Storage
Temperature: 2.0°C
```

---

## Key Concepts Summary

### Producer
- Use `KafkaTemplate` to send messages
- Configure serializers (String, JSON, etc.)
- Can send fire-and-forget or with callbacks
- Messages are automatically partitioned

### Consumer
- Use `@KafkaListener` annotation
- Configure deserializers
- Automatic polling and offset management
- Multiple patterns: load balancing, broadcasting

### Configuration
- `bootstrap-servers`: Where Kafka is running
- `group-id`: Consumer group for coordination
- `auto-offset-reset`: Where to start reading (earliest/latest)
- Serializers/Deserializers: How to convert data

---

## Best Practices

1. **Use JSON for complex objects** - Easy to debug and version
2. **Set consumer group IDs** - Required for offset management
3. **Handle errors gracefully** - Use try-catch and error topics
4. **Use callbacks for producers** - Know if messages were sent successfully
5. **Configure auto-offset-reset** - Decide behavior for new consumers
6. **Add trusted packages** - Required for JSON deserialization security

---

## Next Steps

Now that you know how to use Kafka with Spring Boot, you're ready to build the Fleet Management System project!

Proceed to: **03_PROJECT_REQUIREMENTS.md**
