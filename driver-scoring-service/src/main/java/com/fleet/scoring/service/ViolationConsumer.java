package com.fleet.scoring.service;

import com.fleet.scoring.model.Violation;
import com.fleet.scoring.model.ViolationEvent;
import com.fleet.scoring.repository.ViolationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.UUID;

@Service
public class ViolationConsumer {


    private static final Logger log = LoggerFactory.getLogger(ViolationConsumer.class);
    private final ViolationRepository violationRepository;

    public ViolationConsumer(ViolationRepository violationRepository) {
        this.violationRepository = violationRepository;
    }

    @KafkaListener(topics = "${kafka.topic.violation-event}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeViolation(ViolationEvent event){

        try {
            Violation violation = new Violation();
            violation.setId(UUID.randomUUID());
            violation.setTruckId(event.getTruckId());
            violation.setDriverId(event.getDriverId());
            violation.setEventType(event.getViolationType());
            violation.setSeverity(event.getSeverity());
            violation.setMessage(event.getMessage());
            violation.setSpeed(event.getOriginalData().getSpeed());
            violation.setFuelLevel((event.getOriginalData().getFuelLevel()));
            violation.setEngineTemp(event.getOriginalData().getEngineTemp());
            violation.setLocation_lat(event.getOriginalData().getLocation().getLatitude());
            violation.setLocation_lon(event.getOriginalData().getLocation().getLongitude());
            violation.setCreatedAt(event.getDetectedAt());
            violation.setTimestamp(new Timestamp(System.currentTimeMillis()));
            if (event.getSeverity().equals( "CRITICAL")) {
                violation.setPointsDeducted(4);
            } else if (event.getSeverity().equals( "HIGH")) {
                violation.setPointsDeducted(3);
            } else if (event.getSeverity().equals( "MEDIUM")) {
                violation.setPointsDeducted(2);
            } else if (event.getSeverity().equals( "LOW")) {
                violation.setPointsDeducted(1);
            }
            violationRepository.save(violation);
            log.info("violation event saved in the database.", violation);
        }catch(Exception e){
            log.error("❌ Failed to save violation data in the database: {}", e.getMessage());
        }
    }

}
