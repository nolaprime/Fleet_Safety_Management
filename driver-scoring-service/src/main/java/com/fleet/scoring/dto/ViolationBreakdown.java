package com.fleet.scoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViolationBreakdown {
    private int speeding;
    private int lowTirePressure;
    private int lowFuel;
    private int highTemp;
}
