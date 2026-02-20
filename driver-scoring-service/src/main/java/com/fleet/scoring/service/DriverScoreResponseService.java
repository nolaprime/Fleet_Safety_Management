package com.fleet.scoring.service;

import com.fleet.scoring.dto.*;
import com.fleet.scoring.model.DriverScore;
import com.fleet.scoring.model.Violation;
import com.fleet.scoring.repository.DriverScoreRepository;
import com.fleet.scoring.repository.ViolationRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DriverScoreResponseService {

    private final DriverScoreRepository driverScoreRepository;

    private final ViolationRepository violationRepository;

    public DriverScoreResponseService(DriverScoreRepository driverScoreRepository, ViolationRepository violationRepository) {
        this.driverScoreRepository = driverScoreRepository;
        this.violationRepository = violationRepository;
    }


    public DriverScoreResponse getDriverScoreDetails(String driverId,Pageable pageable){

        DriverScoreResponse driverScoreResponse = new DriverScoreResponse();
        DriverScore driverScore = driverScoreRepository.findByDriverId(driverId);
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Violation> violationsInLast30Days = violationRepository.findAllByDriverIdAndCreatedAtAfter(driverId, thirtyDaysAgo, pageable);

        driverScoreResponse.setDriverId(driverId);
        driverScoreResponse.setCurrentScore(driverScore.getCurrentScore());
        driverScoreResponse.setScoreCategory(driverScore.getScoreCategory());
        int numOfViolationsInLast30Days = Math.toIntExact(violationsInLast30Days.stream().count());
        driverScoreResponse.setTotalViolations(numOfViolationsInLast30Days);
        driverScoreResponse.setLastViolationDate(driverScore.getLastViolationDate());

        int numberOfSpeeding = Math.toIntExact(violationsInLast30Days.stream().filter(v -> "SPEEDING".
                equals(v.getEventType())).count());
        int numberOfLowTirePressure = Math.toIntExact(violationsInLast30Days.stream().filter(v -> "SPEEDING".
                equals(v.getEventType())).count());
        int numberOfLowFuel = Math.toIntExact(violationsInLast30Days.stream().filter(v -> "SPEEDING".
                equals(v.getEventType())).count());
        int numberOfHighTemp = Math.toIntExact(violationsInLast30Days.stream().filter(v -> "SPEEDING".
                equals(v.getEventType())).count());
        driverScoreResponse.setViolationBreakdown(new ViolationBreakdown(numberOfSpeeding,numberOfLowTirePressure, numberOfLowFuel, numberOfHighTemp));

        return driverScoreResponse;
    }

    public ViolationHistoryResponse getAllViolationsInLastNDays(String driverId, int days, Pageable pageable){
        ViolationHistoryResponse violationHistoryResponse = new ViolationHistoryResponse();
        LocalDateTime nDaysAgo = LocalDateTime.now().minusDays(days);
        List<Violation> violations = violationRepository.findAllByDriverIdAndCreatedAtAfter(driverId, nDaysAgo, pageable);

        violationHistoryResponse.setDriverId(driverId);
        violationHistoryResponse.setPeriodDays(days);
        int numOfViolations = Math.toIntExact(violations.stream().count());
        violationHistoryResponse.setTotalViolations(numOfViolations);
        violationHistoryResponse.setViolationList(violations);

        return violationHistoryResponse;
    }

    public LeaderboardResponse getTopPerformingDrivers(int limit, Pageable pageable){
        LeaderboardResponse leaderboardResponse = new LeaderboardResponse();

        List<DriverScore> topDrivers = driverScoreRepository.findByOrderByCurrentScoreDesc(Limit.of(limit));

        List<LeaderBoardDTO> topDriversList = new ArrayList<>();

        for(DriverScore topDriver : topDrivers){
            LeaderBoardDTO leaderBoardDTO = new LeaderBoardDTO();
            leaderBoardDTO.setDriverId(topDriver.getDriverId());
            leaderBoardDTO.setCurrentScore(topDriver.getCurrentScore());
            leaderBoardDTO.setScoreCategory(topDriver.getScoreCategory());
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<Violation> violationsInLast30Days = violationRepository.findAllByDriverIdAndCreatedAtAfter(topDriver.getDriverId(), thirtyDaysAgo, pageable);
            int numOfViolations = Math.toIntExact(violationsInLast30Days.stream().count());
            leaderBoardDTO.setTotalViolations(numOfViolations);
            topDriversList.add(leaderBoardDTO);
        }
        leaderboardResponse.setLeaderBoardDTOList(topDriversList);

        return leaderboardResponse;
    }

    public BottomDriversResponse getBadPerformingDrivers(int limit, Pageable pageable){
        BottomDriversResponse bottomDriversResponse = new BottomDriversResponse();

        List<DriverScore> bottomDrivers = driverScoreRepository.findByOrderByCurrentScoreAsc(Limit.of(limit));

        List<DriverNeedingAttentionDTO> bottomDriversList = new ArrayList<>();

        for(DriverScore bottomDriver : bottomDrivers){
            DriverNeedingAttentionDTO driverNeedingAttentionDTO = new DriverNeedingAttentionDTO();
            driverNeedingAttentionDTO.setDriverId(bottomDriver.getDriverId());
            driverNeedingAttentionDTO.setCurrentScore(bottomDriver.getCurrentScore());
            driverNeedingAttentionDTO.setScoreCategory(bottomDriver.getScoreCategory());
            driverNeedingAttentionDTO.setRecommendedAction("Immediate suspension and retraining required");
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<Violation> violationsInLast30Days = violationRepository.findAllByDriverIdAndCreatedAtAfter(bottomDriver.getDriverId(), thirtyDaysAgo, pageable);
            int numOfViolations = Math.toIntExact(violationsInLast30Days.stream().count());
            driverNeedingAttentionDTO.setTotalViolations(numOfViolations);
            bottomDriversList.add(driverNeedingAttentionDTO);
        }
        bottomDriversResponse.setDriverNeedingAttentionDTOList(bottomDriversList);

        return bottomDriversResponse;
    }
}
