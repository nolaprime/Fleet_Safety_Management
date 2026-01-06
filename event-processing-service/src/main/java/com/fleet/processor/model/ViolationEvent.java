package com.fleet.processor.model;

import jakarta.validation.constraints.*;

import java.util.UUID;


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
    private TelemetryData originalData;
    private Long detectedAt;

    public ViolationEvent() {
    }

    public ViolationEvent(UUID violationId, String truckId, String driverId, String violationType, String severity, String message, TelemetryData originalData, Long detectedAt) {
        this.violationId = violationId;
        this.truckId = truckId;
        this.driverId = driverId;
        this.violationType = violationType;
        this.severity = severity;
        this.message = message;
        this.originalData = originalData;
        this.detectedAt = detectedAt;
    }

    public UUID getViolationId() {
        return violationId;
    }

    public void setViolationId(UUID violationId) {
        this.violationId = violationId;
    }

    public String getTruckId() {
        return truckId;
    }

    public void setTruckId(String truckId) {
        this.truckId = truckId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TelemetryData getOriginalData() {
        return originalData;
    }

    public void setOriginalData(TelemetryData originalData) {
        this.originalData = originalData;
    }

    public Long getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(Long detectedAt) {
        this.detectedAt = detectedAt;
    }
}
