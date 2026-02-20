package com.fleet.scoring.service;

import com.fleet.scoring.model.DriverScore;
import com.fleet.scoring.model.Violation;
import com.fleet.scoring.model.ViolationEvent;
import com.fleet.scoring.repository.ViolationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import java.sql.Timestamp;
import java.util.UUID;

@Service
public class ViolationConsumer {


    private static final Logger log = LoggerFactory.getLogger(ViolationConsumer.class);
    private final ViolationRepository violationRepository;
    private final DriverScoringService driverScoringService;
    private Pageable pageable;

    public ViolationConsumer(ViolationRepository violationRepository, DriverScoringService driverScoringService ) {
        this.violationRepository = violationRepository;
        this.driverScoringService = driverScoringService;
    }

    @KafkaListener(topics = "${kafka.topic.violation-event}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeViolation(ViolationEvent event){

//        DriverScoringService driverScoringService = new DriverScoringService();
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
            violation.setFront_right(event.getOriginalData().getTirePressure().getFrontRight());
            violation.setFront_left(event.getOriginalData().getTirePressure().getFrontLeft());
            violation.setBack_right(event.getOriginalData().getTirePressure().getRearRight());
            violation.setBack_left(event.getOriginalData().getTirePressure().getRearLeft());
            violation.setLocation_lat(event.getOriginalData().getLocation().getLatitude());
            violation.setLocation_lon(event.getOriginalData().getLocation().getLongitude());
            violation.setCreatedAt(event.getDetectedAt());
            violation.setTimestamp(new Timestamp(System.currentTimeMillis()));
            if(event.getViolationType().equals("SPEEDING")) {
                if (event.getSeverity().equals("HIGH")) {
                    violation.setPointsDeducted(5);
                }else if(event.getSeverity().equals("MEDIUM")){
                    violation.setPointsDeducted(2);
                }
            }
            if(event.getViolationType().equals("LOW FUEL")) {
                if (event.getSeverity().equals("CRITICAL")) {
                    violation.setPointsDeducted(3);
                }else if(event.getSeverity().equals("HIGH")){
                    violation.setPointsDeducted(1);
                }
            }
            if(event.getViolationType().equals("HIGH_TEMP")) {
                if (event.getSeverity().equals("CRITICAL")) {
                    violation.setPointsDeducted(5);
                }else if(event.getSeverity().equals("HIGH")){
                    violation.setPointsDeducted(3);
                }
            }
            if(event.getViolationType().equals("LOW_TIRE_PRESSURE")) {
                    violation.setPointsDeducted(2);
            }

            violationRepository.save(violation);
            driverScoringService.calculateScore(violation.getDriverId(), pageable);

            log.info("violation event saved in the database.", violation);
        }catch(Exception e){
            log.error("❌ Failed to save violation data in the database: {}", e.getMessage());
        }
    }

}
