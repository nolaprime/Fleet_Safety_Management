package com.fleet.processor.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TirePressure {
    @NotNull
    @Min(20)
    @Max(120)
    private Double frontLeft;
    @NotNull
    @Min(20)
    @Max(120)
    private Double frontRight;
    @NotNull
    @Min(20)
    @Max(120)
    private Double rearLeft;
    @NotNull
    @Min(20)
    @Max(120)
    private Double rearRight;
}