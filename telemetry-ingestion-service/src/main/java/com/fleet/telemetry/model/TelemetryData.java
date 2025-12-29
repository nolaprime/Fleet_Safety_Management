package com.fleet.telemetry.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple Telemetry Data Model
 * 
 * For learning purposes, we'll focus on one basic metric: speed
 * Real implementation would include more fields like GPS, acceleration, etc.
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
     * Speed in km/h - the basic metric we're tracking
     */
    private Double speed;
    
    /**
     * Timestamp when the data was collected
     */
    private Long timestamp;
}
