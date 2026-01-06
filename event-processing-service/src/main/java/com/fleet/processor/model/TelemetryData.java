package com.fleet.processor.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
     * Speed in km/h - the basic metric we're tracking
     */
    @NotNull
    @Min(0)
    @Max(200)
    private Double speed;
    
    /**
     * Timestamp when the data was collected
     */
    private Long timestamp;

    @NotNull
    private String driverId;

    @NotNull
    @Min(0)
    @Max(100)
    private Double fuelLevel;

    @NotNull
    @Min(0)
    @Max(150)
    private Double engineTemp;

    private Location location;
    private TirePressure tirePressure;
}
