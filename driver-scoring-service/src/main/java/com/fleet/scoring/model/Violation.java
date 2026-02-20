package com.fleet.scoring.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenerationTime;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "violations")
public class Violation {
    @Id
    UUID id;
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
    BigDecimal front_right;
    BigDecimal front_left;
    BigDecimal back_right;
    BigDecimal back_left;
    Integer pointsDeducted;
    Timestamp createdAt;
    Timestamp timestamp;
}
