package com.fleet.telemetry.service;

import com.fleet.telemetry.model.TelemetryData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Telemetry Producer Service
 * 
 * This service is responsible for sending telemetry data to Kafka.
 * 
 * Key Learning Points:
 * - Using KafkaTemplate to send messages
 * - Understanding topics
 * - Message keys for partitioning
 * - Fire-and-forget pattern
 */
@Service
@Slf4j
public class TelemetryProducer {

    private final KafkaTemplate<String, TelemetryData> kafkaTemplate;
    
    @Value("${kafka.topic.raw-telemetry}")
    private String rawTelemetryTopic;

    public TelemetryProducer(KafkaTemplate<String, TelemetryData> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Send telemetry data to Kafka
     * 
     * This method demonstrates the "fire-and-forget" pattern:
     * - We send the message to Kafka
     * - We don't wait for confirmation
     * - This is fast but less reliable
     * 
     * @param telemetryData The telemetry data to send
     */
    public void sendTelemetry(TelemetryData telemetryData) {
        try {
            // Send the message to Kafka
            // Parameters:
            // 1. Topic name: where to send the message
            // 2. Key: used for partitioning (messages with same key go to same partition)
            // 3. Value: the actual data to send
            kafkaTemplate.send(rawTelemetryTopic, telemetryData.getTruckId(), telemetryData);
            
            log.info("✅ Telemetry data sent to Kafka - Truck: {}, Speed: {} km/h", 
                    telemetryData.getTruckId(), 
                    telemetryData.getSpeed());
                    
        } catch (Exception e) {
            log.error("❌ Failed to send telemetry data to Kafka: {}", e.getMessage());
        }
    }
}
