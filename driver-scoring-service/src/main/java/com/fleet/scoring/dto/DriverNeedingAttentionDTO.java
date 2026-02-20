package com.fleet.scoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverNeedingAttentionDTO {
    private String driverId;
    private int currentScore;
    private String scoreCategory;
    private int totalViolations;
    private String recommendedAction;
}
