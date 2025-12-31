package com.fleet.telemetry.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// @Data
// @NoArgsConstructor
// @AllArgsConstructor
public class TirePressure {
    private Double frontLeft;
    private Double frontRight;
    private Double rearLeft;
    private Double rearRight;
}