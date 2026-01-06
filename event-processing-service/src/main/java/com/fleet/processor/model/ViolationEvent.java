package com.fleet.processor.model;

import java.util.UUID;


public class ViolationEvent {
    private UUID violationId;
    @NotNull
    private String truckId;
    private String driverId;
    private String violationType;    // SPEEDING, LOW_FUEL, HIGH_TEMP, LOW_TIRE_PRESSURE
    private String severity;         // CRITICAL, HIGH, MEDIUM, LOW
    private String message;
    private TelemetryData originalData;
    private Long detectedAt;

}
