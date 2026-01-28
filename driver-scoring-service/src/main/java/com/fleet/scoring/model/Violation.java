package com.fleet.scoring.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "violations")
public class Violation {
    @Id
    UUID id;
    UUID tripId;
    String truckId;
    String driverId;
    String eventType;
    String severity;
    String message;
    BigDecimal speed;
    BigDecimal fuelLevel;
    BigDecimal engineTemp;
    BigDecimal location_lat;
    BigDecimal  location_lon;
    Integer pointsDeducted;
    Timestamp createdAt;
}
