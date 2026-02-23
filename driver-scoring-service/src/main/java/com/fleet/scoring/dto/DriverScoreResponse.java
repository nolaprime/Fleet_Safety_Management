package com.fleet.scoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverScoreResponse {
    private String driverId;
    private int currentScore;
    private String scoreCategory;
    private int totalViolations;
    private Timestamp lastViolationDate;
    private ViolationBreakdown violationBreakdown;
}
