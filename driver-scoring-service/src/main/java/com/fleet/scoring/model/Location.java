package com.fleet.scoring.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private BigDecimal latitude;
    private BigDecimal longitude;

}
