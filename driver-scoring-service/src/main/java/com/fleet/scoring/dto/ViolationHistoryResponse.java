package com.fleet.scoring.dto;

import com.fleet.scoring.model.Violation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViolationHistoryResponse {
    private String driverId;
    private int periodDays;
    private int totalViolations;
    private List<Violation> violationList;
}
