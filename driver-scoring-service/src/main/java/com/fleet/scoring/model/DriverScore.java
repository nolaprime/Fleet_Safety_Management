package com.fleet.scoring.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "driver_score")
public class DriverScore {
    @Id
    UUID scoreId;
    String driverId;

    Integer currentScore;
    String scoreCategory;
    Integer totalViolations;
    Timestamp lastViolationDate;
    Timestamp updatedAt;
}
