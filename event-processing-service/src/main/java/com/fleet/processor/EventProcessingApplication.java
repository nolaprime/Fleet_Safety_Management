package com.fleet.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Event Processing Service - Kafka Consumer
 * 
 * This service consumes telemetry messages from Kafka and processes them.
 * 
 * Learning Objectives:
 * - Understanding Kafka Consumer patterns
 * - Consuming messages from topics
 * - Processing streaming data
 */
@SpringBootApplication
public class EventProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventProcessingApplication.class, args);
    }
}
