package com.fleet.telemetry.model;

import jakarta.validation.constraints.*;
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
    @NotNull
    @Min(value = 0, message = "minimum speed is 0 Km/h")
    @Max(value = 200, message = "maximum speed is 200 Km/h")
    private Double speed;
    
    /**
     * Timestamp when the data was collected
     */
    private Long timestamp;

    @NotNull
    private String driverId;

    @NotNull
    @Min(value = 0, message = "minimum fuel level is 0%")
    @Max(value = 100, message = "maximum fuel level is 100%")
    private Double fuelLevel;

    @NotNull
    @Min(value = 0, message = "minimum engine temperature is 0 C")
    @Max(value = 150, message = "maximum engine temperature is 150 C")
    private Double engineTemp;

    private Location location;
    private TirePressure tirePressure;
}
