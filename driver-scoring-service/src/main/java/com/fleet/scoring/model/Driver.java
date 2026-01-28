package com.fleet.scoring.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "drivers")
public class Driver {
    @Id
    String id;
    String name;
    Integer currentScore;
    Integer totalTrips;
    BigDecimal totalMiles;
    Integer totalViolations;
    Timestamp createdAt;
    Timestamp updatedAt;
}
