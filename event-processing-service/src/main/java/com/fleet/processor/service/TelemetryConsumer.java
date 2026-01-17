package com.fleet.processor.service;

import com.fleet.processor.model.TelemetryData;
import com.fleet.processor.model.ViolationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
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

    final KafkaTemplate<String, ViolationEvent> kafkaTemplate;

    @Value("${kafka.topic.violation-event}")
    private String violationEventTopic;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public TelemetryConsumer(KafkaTemplate<String, ViolationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private ViolationProducer violationProducer;

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
            if (telemetryData.getLocation() != null) {
                log.info("   └─ Location: lat {}, long {}",
                        telemetryData.getLocation().getLatitude(),
                        telemetryData.getLocation().getLongitude());
            } else {
                log.info("   └─ Location: N/A");
            }
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
                ViolationEvent violationEvent = new ViolationEvent();
                violationEvent.setTruckId(telemetryData.getTruckId());
                violationEvent.setDriverId(telemetryData.getDriverId());
                violationEvent.setViolationType("SPEEDING");
                violationEvent.setMessage("Truck " + telemetryData.getTruckId() + "speeding at " + telemetryData.getSpeed() + " km/h (Driver: " + telemetryData.getDriverId() + ")");
                violationEvent.setOriginalData(telemetryData);
                if(telemetryData.getSpeed() <= 100){
                    violationEvent.setSeverity("MEDIUM");
                }
                else{
                    violationEvent.setSeverity("HIGH");
                }
                log.info("Event details: ", violationEvent);
                try{
                    violationProducer.sendViolation(violationEvent);
                    log.info("Violation data sent to Kafka topic: ", violationEvent);
                }catch(Exception e) {
                    log.error("❌ Failed to send violation data to Kafka: {}", e.getMessage());
                }

            }

            if(telemetryData.getFuelLevel() < 15){
                ViolationEvent violationEvent = new ViolationEvent();
                violationEvent.setTruckId(telemetryData.getTruckId());
                violationEvent.setDriverId(telemetryData.getDriverId());
                violationEvent.setViolationType("LOW FUEL");
                violationEvent.setMessage("Truck " + telemetryData.getTruckId() + "low fuel " + telemetryData.getFuelLevel() + " %");
                violationEvent.setOriginalData(telemetryData);
                if(telemetryData.getFuelLevel() >= 5){
                    violationEvent.setSeverity("HIGH");
                }
                else{
                    violationEvent.setSeverity("CRITICAL");
                }
                log.info("Event details: ", violationEvent);
                try{
                    violationProducer.sendViolation(violationEvent);
                    log.info("Violation data sent to Kafka topic: ", violationEvent);
                }catch(Exception e) {
                    log.error("❌ Failed to send violation data to Kafka: {}", e.getMessage());
                }
            }

            if(telemetryData.getEngineTemp() > 110){
                ViolationEvent violationEvent = new ViolationEvent();
                violationEvent.setTruckId(telemetryData.getTruckId());
                violationEvent.setDriverId(telemetryData.getDriverId());
                violationEvent.setViolationType("HIGH_TEMP");
                violationEvent.setMessage("Truck " + telemetryData.getTruckId() + "high engine temperature " + telemetryData.getEngineTemp() + "°C");
                violationEvent.setOriginalData(telemetryData);
                if(telemetryData.getFuelLevel() <= 120){
                    violationEvent.setSeverity("HIGH");
                }
                else{
                    violationEvent.setSeverity("CRITICAL");
                }
                log.info("Event details: ", violationEvent);
                try{
                    violationProducer.sendViolation(violationEvent);
                    log.info("Violation data sent to Kafka topic: ", violationEvent);
                }catch(Exception e) {
                    log.error("❌ Failed to send violation data to Kafka: {}", e.getMessage());
                }
            }

            if(telemetryData.getTirePressure().getFrontLeft() < 28){
                ViolationEvent violationEvent = new ViolationEvent();
                violationEvent.setTruckId(telemetryData.getTruckId());
                violationEvent.setDriverId(telemetryData.getDriverId());
                violationEvent.setViolationType("LOW_TIRE_PRESSURE");
                violationEvent.setMessage("Truck " + telemetryData.getTruckId() + "low tire pressure: front left tire = " + telemetryData.getTirePressure().getFrontLeft() + "PSI");
                violationEvent.setOriginalData(telemetryData);
                violationEvent.setSeverity("CRITICAL");
                log.info("Event details: ", violationEvent);
                try{
                    violationProducer.sendViolation(violationEvent);
                    log.info("Violation data sent to Kafka topic: ", violationEvent);
                }catch(Exception e) {
                    log.error("❌ Failed to send violation data to Kafka: {}", e.getMessage());
                }
            }

            if(telemetryData.getTirePressure().getFrontRight() < 28){
                ViolationEvent violationEvent = new ViolationEvent();
                violationEvent.setTruckId(telemetryData.getTruckId());
                violationEvent.setDriverId(telemetryData.getDriverId());
                violationEvent.setViolationType("LOW_TIRE_PRESSURE");
                violationEvent.setMessage("Truck " + telemetryData.getTruckId() + "low tire pressure: front right tire = " + telemetryData.getTirePressure().getFrontRight() + "PSI");
                violationEvent.setOriginalData(telemetryData);
                violationEvent.setSeverity("CRITICAL");
                log.info("Event details: ", violationEvent);
                try{
                    violationProducer.sendViolation(violationEvent);
                    log.info("Violation data sent to Kafka topic: ", violationEvent);
                }catch(Exception e) {
                    log.error("❌ Failed to send violation data to Kafka: {}", e.getMessage());
                }
            }

            if(telemetryData.getTirePressure().getRearLeft() < 28){
                ViolationEvent violationEvent = new ViolationEvent();
                violationEvent.setTruckId(telemetryData.getTruckId());
                violationEvent.setDriverId(telemetryData.getDriverId());
                violationEvent.setViolationType("LOW_TIRE_PRESSURE");
                violationEvent.setMessage("Truck " + telemetryData.getTruckId() + "low tire pressure: back left tire = " + telemetryData.getTirePressure().getRearLeft() + "PSI");
                violationEvent.setOriginalData(telemetryData);
                violationEvent.setSeverity("CRITICAL");
                log.info("Event details: ", violationEvent);
                try{
                    violationProducer.sendViolation(violationEvent);
                    log.info("Violation data sent to Kafka topic: ", violationEvent);
                }catch(Exception e) {
                    log.error("❌ Failed to send violation data to Kafka: {}", e.getMessage());
                }
            }

            if(telemetryData.getTirePressure().getRearRight() < 28){
                ViolationEvent violationEvent = new ViolationEvent();
                violationEvent.setTruckId(telemetryData.getTruckId());
                violationEvent.setDriverId(telemetryData.getDriverId());
                violationEvent.setViolationType("LOW_TIRE_PRESSURE");
                violationEvent.setMessage("Truck " + telemetryData.getTruckId() + "low tire pressure: back right tire = " + telemetryData.getTirePressure().getRearRight() + "PSI");
                violationEvent.setOriginalData(telemetryData);
                violationEvent.setSeverity("CRITICAL");
                log.info("Event details: ", violationEvent);
                try{
                    violationProducer.sendViolation(violationEvent);
                    log.info("Violation data sent to Kafka topic: ", violationEvent);
                }catch(Exception e) {
                    log.error("❌ Failed to send violation data to Kafka: {}", e.getMessage());
                }
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
