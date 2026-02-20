package com.fleet.scoring.service;

import com.fleet.scoring.model.DriverScore;
import com.fleet.scoring.model.Violation;
import com.fleet.scoring.repository.DriverScoreRepository;
import com.fleet.scoring.repository.ViolationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Pageable;
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


    public DriverScoringService(ViolationRepository violationRepository, DriverScoreRepository driverScoreRepository) {
        this.violationRepository = violationRepository;
        this.driverScoreRepository = driverScoreRepository;
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
