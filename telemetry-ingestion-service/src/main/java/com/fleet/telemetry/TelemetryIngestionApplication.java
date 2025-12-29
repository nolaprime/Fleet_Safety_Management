package com.fleet.telemetry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Telemetry Ingestion Service - Kafka Producer
 * 
 * This service receives telemetry data from IoT devices (trucks)
 * and publishes it to Kafka for downstream processing.
 * 
 * Learning Objectives:
 * - Understanding Kafka Producer patterns
 * - Fire-and-forget messaging
 * - REST API to Kafka integration
 */
@SpringBootApplication
public class TelemetryIngestionApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelemetryIngestionApplication.class, args);
    }
}
