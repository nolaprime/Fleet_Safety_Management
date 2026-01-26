package com.fleet.scoring.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViolationEvent {
    private UUID violationId;
    @NotNull
    private String truckId;
    @NotNull
    private String driverId;
    @NotNull
    @Pattern(regexp = "SPEEDING|LOW_FUEL|HIGH_TEMP|LOW_TIRE_PRESSURE", message = "Violations are speeding, low fuel, high engine temperature and/or low tire pressure")
    private String violationType;    // SPEEDING, LOW_FUEL, HIGH_TEMP, LOW_TIRE_PRESSURE
    @NotNull
    @Pattern(regexp = "CRITICAL|HIGH|MEDIUM|LOW")
    private String severity;         // CRITICAL, HIGH, MEDIUM, LOW
    private String message;
    @NotNull
//    private TelemetryData originalData;
    private Long detectedAt;


}
