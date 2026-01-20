package com.fleet.processor.service;

import com.fleet.processor.model.ViolationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ViolationProducer {
    private final KafkaTemplate<String, ViolationEvent> kafkaTemplate;

    @Value("${kafka.topic.violation-event}")
    private String violationEventTopic;

    public ViolationProducer(KafkaTemplate<String, ViolationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendViolation(ViolationEvent violationEvent) {
        try {
            kafkaTemplate.send(violationEventTopic, violationEvent.getTruckId(), violationEvent);
        }
        catch (Exception e) {
            log.error("❌ Failed to send violation data to Kafka: {}", e.getMessage());
        }
    }
}
