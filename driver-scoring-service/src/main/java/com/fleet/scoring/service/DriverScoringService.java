package com.fleet.scoring.service;

import com.fleet.scoring.model.Driver;
import com.fleet.scoring.model.DriverScore;
import com.fleet.scoring.model.Violation;
import com.fleet.scoring.repository.DriverRepository;
import com.fleet.scoring.repository.DriverScoreRepository;
import com.fleet.scoring.repository.ViolationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class DriverScoringService {



    private final ViolationRepository violationRepository;
    private final DriverScoreRepository driverScoreRepository;
    private DriverRepository driverRepository;


    public DriverScoringService(ViolationRepository violationRepository, DriverScoreRepository driverScoreRepository, DriverRepository driverRepository) {
        this.violationRepository = violationRepository;
        this.driverScoreRepository = driverScoreRepository;
        this.driverRepository = driverRepository;
    }

    @Transactional
    public void ensureDriverExists(String driverId, Timestamp lastViolationTimestamp) {
        try{
            if (!driverRepository.existsById(driverId)) {
                // Create driver record
                Driver driver = new Driver();
                driver.setId(driverId);
                driver.setName("Driver " + driverId); // Placeholder name
                driver.setCurrentScore(100);
                driver.setTotalTrips(0);
                driver.setTotalMiles(BigDecimal.ZERO);
                driver.setTotalViolations(0);
                driverRepository.saveAndFlush(driver);

                // Create initial score record
                DriverScore score = new DriverScore();
                score.setScoreId(UUID.randomUUID());
                score.setDriverId(driver.getId());
                score.setCurrentScore(100);
                score.setScoreCategory("EXCELLENT");
                score.setTotalViolations(0);
                score.setLastViolationDate(lastViolationTimestamp);
                driverScoreRepository.save(score);

                log.info("Created new driver: {}", driverId);
            }
        }  catch (DataIntegrityViolationException e) {
            log.warn("Driver {} was created by another thread concurrently", driverId);
        } catch (Exception e) {
            log.error("Failed to persist: ", e.getMessage());
        }
    }

    public DriverScore calculateScore(String driverId, Pageable pageable) {

        DriverScore driverScore = new DriverScore();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        int score = 100;
        int numOfViolations = 0;
        List<Violation> violationItems = violationRepository.findAllByDriverIdAndCreatedAtAfter(driverId, thirtyDaysAgo, pageable);

        for(Violation items : violationItems){
            score = score - items.getPointsDeducted();
            numOfViolations = numOfViolations + 1 ;
        }
        if(score<0){
            score = 0;
        }
        driverScore.setCurrentScore(score);
        if(score > 90){
            driverScore.setScoreCategory("EXCELLENT");
        }else if(score > 75){
            driverScore.setScoreCategory("GOOD");
        }else if(score > 60){
            driverScore.setScoreCategory("AVERAGE");
        }else if(score > 40){
            driverScore.setScoreCategory("POOR");
        }else if(score < 40){
            driverScore.setScoreCategory("CRITICAL");
        }
        driverScore.setDriverId(driverId);
        driverScore.setScoreId(UUID.randomUUID());
        driverScore.setTotalViolations(numOfViolations);
        Timestamp latestDate = violationItems.stream()
                .map(Violation::getCreatedAt)
                .max(Timestamp::compareTo)
                .orElse(null);
        driverScore.setLastViolationDate(latestDate);
        driverScore.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        driverScoreRepository.save(driverScore);
        log.info("Driver score updated: driver ID = " + driverId + " new score = " + driverScore.getCurrentScore() + " updated at " + driverScore.getUpdatedAt());
        return driverScore;
    }
}
