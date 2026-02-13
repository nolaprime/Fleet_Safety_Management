package com.fleet.scoring.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TirePressure {
    @NotNull
    @Min(20)
    @Max(120)
    private BigDecimal frontLeft;
    @NotNull
    @Min(20)
    @Max(120)
    private BigDecimal frontRight;
    @NotNull
    @Min(20)
    @Max(120)
    private BigDecimal rearLeft;
    @NotNull
    @Min(20)
    @Max(120)
    private BigDecimal rearRight;
}