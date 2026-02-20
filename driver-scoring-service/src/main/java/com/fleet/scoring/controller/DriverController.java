package com.fleet.scoring.controller;

import com.fleet.scoring.dto.BottomDriversResponse;
import com.fleet.scoring.dto.DriverScoreResponse;
import com.fleet.scoring.dto.LeaderboardResponse;
import com.fleet.scoring.dto.ViolationHistoryResponse;
import com.fleet.scoring.repository.DriverScoreRepository;
import com.fleet.scoring.repository.ViolationRepository;
import com.fleet.scoring.service.DriverScoreResponseService;
import com.fleet.scoring.service.DriverScoringService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
@Validated
public class DriverController {
    private DriverScoringService scoringService;

    DriverScoreResponseService driverScoreResponseService;

    DriverScoreRepository driverScoreRepository;

    ViolationRepository violationRepository;
    public DriverController(DriverScoreResponseService driverScoreResponseService,
                            DriverScoreRepository driverScoreRepository,
                            DriverScoringService driverScoringService
    ) {
        this.driverScoreRepository = driverScoreRepository;
        this.driverScoreResponseService = driverScoreResponseService;
        this.scoringService = driverScoringService;
    }



    @GetMapping("/{driverId}/score")
    public ResponseEntity<Object> getScore(@PathVariable
                                                            @NotBlank(message = "Driver ID cannot be empty")
                                                            @Pattern(regexp = "^DRV-[A-Z]{3}-\\d{3}$", message = "Driver ID must start with 'DRV-'")
                                                            String driverId,
                                                        Pageable pageable) {
        try {
            if (driverScoreRepository.findByDriverId(driverId) == null) {
                Map<String, Object> resposne = new HashMap<>();
                resposne.put("message", "Driver not found with Id: " + driverId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposne);
            }

            return ResponseEntity.ok(driverScoreResponseService.getDriverScoreDetails(driverId, pageable));

        } catch (ConstraintViolationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{driverId}/violations")
    public ResponseEntity<ViolationHistoryResponse> getViolations(
            @PathVariable String driverId,
            @RequestParam(defaultValue = "30")
            @NotBlank
            @Positive @Max(1000) int days,
            Pageable pageable){
        try {
            //using driverScoreRepository to check if driver exists, may need to be changed
            if (driverScoreRepository.findByDriverId(driverId) == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(driverScoreResponseService.getAllViolationsInLastNDays(driverId, days, pageable));
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<LeaderboardResponse> getLeaderboard(
            @RequestParam(defaultValue = "10")
            @NotBlank
            @Positive @Max(50) int limit, Pageable pageable) {
        try {
            return ResponseEntity.ok(driverScoreResponseService.getTopPerformingDrivers(limit, pageable));
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/bottom")
    public ResponseEntity<BottomDriversResponse> getBottomDrivers(
            @RequestParam(defaultValue = "10")
            @NotBlank
            @Positive @Max(50) int limit, Pageable pageable){
        try {
            return ResponseEntity.ok(driverScoreResponseService.getBadPerformingDrivers(limit, pageable));
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}