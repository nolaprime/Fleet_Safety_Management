package com.fleet.processor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Telemetry Data Model (Consumer side)
 * 
 * This mirrors the TelemetryData model from the producer.
 * In a real microservices architecture, you might share models via a common library,
 * but for learning purposes, we're keeping services independent.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryData {
    
    /**
     * Unique identifier for the truck
     */
    private String truckId;
    
    /**
     * Speed in km/h
     */
    private Double speed;
    
    /**
     * Timestamp when the data was collected
     */
    private Long timestamp;
}
