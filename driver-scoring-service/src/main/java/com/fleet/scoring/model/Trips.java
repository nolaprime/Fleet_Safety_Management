package com.fleet.scoring.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "trips")
public class Trips {
    @Id
    UUID id;
    String driverId;
    String truckId;
    String deviceId;
    Timestamp startTime;
    Timestamp endTime;
    Integer durationMinutes;
    BigDecimal startLocationLat;
    BigDecimal startLocationLon;
    BigDecimal endLocationLat;
    BigDecimal endLocationLon;
    BigDecimal totalMiles;
    Integer violationCount;
    String Status;
    Timestamp createdAt;
    Timestamp updatedAt;
}
