package com.fleet.processor.service;

import com.fleet.processor.model.TelemetryData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Telemetry Consumer Service
 * 
 * This service listens to the Kafka topic and processes incoming telemetry messages.
 * 
 * Key Learning Points:
 * - @KafkaListener annotation for automatic message consumption
 * - Processing messages as they arrive
 * - Understanding consumer groups
 * - Message handling patterns
 */
@Service
@Slf4j
public class TelemetryConsumer {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Listen to and process telemetry messages from Kafka
     * 
     * The @KafkaListener annotation tells Spring to automatically:
     * 1. Connect to Kafka
     * 2. Subscribe to the specified topic
     * 3. Poll for new messages
     * 4. Call this method for each message received
     * 
     * Parameters:
     * - topics: The Kafka topic to listen to
     * - groupId: The consumer group (consumers in same group share the load)
     * 
     * @param telemetryData The deserialized message from Kafka
     */
    @KafkaListener(topics = "${kafka.topic.raw-telemetry}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeTelemetry(TelemetryData telemetryData) {
        try {
            // Format the timestamp for better readability
            String formattedTime = dateFormat.format(new Date(telemetryData.getTimestamp()));
            
            // Log the received message
            log.info("📨 Received telemetry from Kafka:");
            log.info("   └─ Truck ID: {}", telemetryData.getTruckId());
            log.info("   └─ Speed: {} km/h", telemetryData.getSpeed());
            log.info("   └─ Timestamp: {}", formattedTime);
            log.info("   └─ Driver ID: {}", telemetryData.getDriverId());
            log.info("   └─ Fuel Level: {}%", telemetryData.getFuelLevel());
            log.info("   └─ Engine Temp: {} °C", telemetryData.getEngineTemp());
            log.info("   └─ Location: lat {}, long {}", telemetryData.getLocation().getLatitude(), telemetryData.getLocation().getLongitude());
            log.info("  └─ Tire Pressure: FL {} psi, FR {} psi, RL {} psi, RR {} psi",
                    telemetryData.getTirePressure().getFrontLeft(),
                    telemetryData.getTirePressure().getFrontRight(),
                    telemetryData.getTirePressure().getRearLeft(),
                    telemetryData.getTirePressure().getRearRight());
            
            // Here you would add your business logic
            // For example:
            // - Validate the data
            // - Check for speed violations
            // - Calculate metrics
            // - Store in database
            // - Publish to another topic
            
            // For now, we'll just add a simple speed check
            if (telemetryData.getSpeed() > 80) {
                log.warn("⚠️  Speed violation detected! Truck {} exceeding speed limit at {} km/h", 
                        telemetryData.getTruckId(), 
                        telemetryData.getSpeed());
            }
            
        } catch (Exception e) {
            log.error("❌ Error processing telemetry data: {}", e.getMessage(), e);
            // In production, you might want to:
            // - Send to a dead-letter queue
            // - Retry the message
            // - Alert monitoring systems
        }
    }
}
