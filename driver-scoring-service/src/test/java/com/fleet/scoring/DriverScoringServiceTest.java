package com.fleet.scoring;

import com.fleet.scoring.model.DriverScore;
import com.fleet.scoring.model.Violation;
import com.fleet.scoring.repository.DriverScoreRepository;
import com.fleet.scoring.repository.ViolationRepository;
import com.fleet.scoring.service.DriverScoringService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DriverScoringServiceTest {

    @Mock
    private ViolationRepository violationRepository;

    @InjectMocks
    private DriverScoringService driverScoringService;

    @Mock
    private DriverScoreRepository driverScoreRepository;

    @Test
    void testNoViolations() {

        Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());
        Timestamp createdAt2 = Timestamp.valueOf(LocalDateTime.now().minusMinutes(2));

        Violation v1 = new Violation(UUID.randomUUID(),
                UUID.randomUUID(),
                "Tr111",
                "Dr111",
                "",
                "",
                "Good Job",
                new BigDecimal("78.0"),
                new BigDecimal("75"),
                new BigDecimal("120"),
                new BigDecimal("8.234"),
                new BigDecimal("34.456"),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                0, createdAt, createdAt);

                List<Violation> violations = List.of(v1);

        when(violationRepository.findAllByDriverIdAndViolationDateAfter(eq(v1.getDriverId()), any()))
                .thenReturn(violations);

        DriverScore driverScore = driverScoringService.calculateScore(v1.getDriverId());
        assertEquals(100, driverScore.getCurrentScore());
        assertEquals("EXCELLENT", driverScore.getScoreCategory());
    }

    @Test
    void test3MediumSpeedings() {

        Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());
        Timestamp createdAt2 = Timestamp.valueOf(LocalDateTime.now().minusMinutes(2));

        Violation v1 = new Violation(UUID.randomUUID(),
                                    UUID.randomUUID(),
                            "Tr111",
                            "Dr111",
                            "SPEEDING",
                            "MEDIUM",
                            "Slow Down",
                            new BigDecimal("88.0"),
                            new BigDecimal("75"),
                            new BigDecimal("120"),
                            new BigDecimal("8.234"),
                            new BigDecimal("34.456"),
                            BigDecimal.valueOf(25),
                            BigDecimal.valueOf(25),
                            BigDecimal.valueOf(25),
                            BigDecimal.valueOf(25),
                            2, createdAt, createdAt);

        Violation v2 = new Violation(UUID.randomUUID(),
                UUID.randomUUID(),
                "Tr111",
                "Dr111",
                "SPEEDING",
                "MEDIUM",
                "Slow Down",
                new BigDecimal("90.0"),
                new BigDecimal("75"),
                new BigDecimal("120"),
                new BigDecimal("8.237"),
                new BigDecimal("34.456"),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                2, createdAt2, createdAt2);

        Violation v3 = new Violation(UUID.randomUUID(),
                UUID.randomUUID(),
                "Tr111",
                "Dr111",
                "SPEEDING",
                "MEDIUM",
                "Slow Down",
                new BigDecimal("99.0"),
                new BigDecimal("75"),
                new BigDecimal("120"),
                new BigDecimal("8.267"),
                new BigDecimal("34.451"),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                2, createdAt2, createdAt2);

        List<Violation> violations = List.of(v1, v2, v3);

        when(violationRepository.findAllByDriverIdAndViolationDateAfter(eq(v1.getDriverId()), any()))
                .thenReturn(violations);

        DriverScore driverScore = driverScoringService.calculateScore(v1.getDriverId());
        assertEquals(94, driverScore.getCurrentScore());
        assertEquals("EXCELLENT", driverScore.getScoreCategory());
    }

    @Test
    void testMixedViolations() {

        Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());
        Timestamp createdAt2 = Timestamp.valueOf(LocalDateTime.now().minusMinutes(2));
        Timestamp createdAt3 = Timestamp.valueOf(LocalDateTime.now().minusMinutes(10));

        Violation v1 = new Violation(UUID.randomUUID(),
                UUID.randomUUID(),
                "Tr111",
                "Dr111",
                "SPEEDING",
                "MEDIUM",
                "Slow Down",
                new BigDecimal("88.0"),
                new BigDecimal("75"),
                new BigDecimal("120"),
                new BigDecimal("8.234"),
                new BigDecimal("34.456"),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                2, createdAt, createdAt);

        Violation v2 = new Violation(UUID.randomUUID(),
                UUID.randomUUID(),
                "Tr111",
                "Dr111",
                "LOW FUEL",
                "CRITICAL",
                "Get Fuel",
                new BigDecimal("90.0"),
                new BigDecimal("5"),
                new BigDecimal("100"),
                new BigDecimal("8.244"),
                new BigDecimal("34.696"),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                3, createdAt2, createdAt2);

        Violation v3 = new Violation(UUID.randomUUID(),
                UUID.randomUUID(),
                "Tr111",
                "Dr111",
                "HIGH_TEMP",
                "CRITICAL",
                "Cool Down Your Engine!",
                new BigDecimal("79.0"),
                new BigDecimal("75"),
                new BigDecimal("125"),
                new BigDecimal("8.267"),
                new BigDecimal("34.451"),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                5, createdAt3, createdAt3);

        Violation v4 = new Violation(UUID.randomUUID(),
                UUID.randomUUID(),
                "Tr111",
                "Dr111",
                "LOW_TIRE_PRESSURE",
                "HIGH",
                "Fix Tire Pressure.",
                new BigDecimal("79.0"),
                new BigDecimal("75"),
                new BigDecimal("109"),
                new BigDecimal("8.267"),
                new BigDecimal("34.451"),
                BigDecimal.valueOf(18),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                2, createdAt3, createdAt3);


        List<Violation> violations = List.of(v1, v2, v3, v4);

        when(violationRepository.findAllByDriverIdAndViolationDateAfter(eq(v1.getDriverId()), any()))
                .thenReturn(violations);

        DriverScore driverScore = driverScoringService.calculateScore(v1.getDriverId());
        assertEquals(88, driverScore.getCurrentScore());
        assertEquals("GOOD", driverScore.getScoreCategory());
    }

    @Test
    void testOldViolations() {

        Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now().minusMonths(2));

        Violation v1 = new Violation(UUID.randomUUID(),
                UUID.randomUUID(),
                "Tr111",
                "Dr111",
                "SPEEDING",
                "HIGH",
                "Slow Down",
                new BigDecimal("108.0"),
                new BigDecimal("75"),
                new BigDecimal("120"),
                new BigDecimal("8.234"),
                new BigDecimal("34.456"),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                5, createdAt, createdAt);

        List<Violation> violations = new ArrayList<>();

        when(violationRepository.findAllByDriverIdAndViolationDateAfter(eq(v1.getDriverId()), any()))
                .thenReturn(violations);

        DriverScore driverScore = driverScoringService.calculateScore(v1.getDriverId());
        assertEquals(100, driverScore.getCurrentScore());
        assertEquals("EXCELLENT", driverScore.getScoreCategory());
    }

    @Test
    void testScoreBelowZero() {

        Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());
        Timestamp createdAt2 = Timestamp.valueOf(LocalDateTime.now().minusMinutes(2));
        Timestamp createdAt3 = Timestamp.valueOf(LocalDateTime.now().minusMinutes(10));

        Violation v1 = new Violation(UUID.randomUUID(),
                UUID.randomUUID(),
                "Tr111",
                "Dr111",
                "SPEEDING",
                "HIGH",
                "Slow Down",
                new BigDecimal("88.0"),
                new BigDecimal("75"),
                new BigDecimal("120"),
                new BigDecimal("8.234"),
                new BigDecimal("34.456"),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                30, createdAt, createdAt);

        Violation v2 = new Violation(UUID.randomUUID(),
                UUID.randomUUID(),
                "Tr111",
                "Dr111",
                "LOW FUEL",
                "CRITICAL",
                "Get Fuel",
                new BigDecimal("90.0"),
                new BigDecimal("5"),
                new BigDecimal("100"),
                new BigDecimal("8.244"),
                new BigDecimal("34.696"),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                30, createdAt2, createdAt2);

        Violation v3 = new Violation(UUID.randomUUID(),
                UUID.randomUUID(),
                "Tr111",
                "Dr111",
                "HIGH_TEMP",
                "CRITICAL",
                "Cool Down Your Engine!",
                new BigDecimal("79.0"),
                new BigDecimal("75"),
                new BigDecimal("125"),
                new BigDecimal("8.267"),
                new BigDecimal("34.451"),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                30, createdAt3, createdAt3);

        Violation v4 = new Violation(UUID.randomUUID(),
                UUID.randomUUID(),
                "Tr111",
                "Dr111",
                "LOW_TIRE_PRESSURE",
                "HIGH",
                "Fix Tire Pressure.",
                new BigDecimal("79.0"),
                new BigDecimal("75"),
                new BigDecimal("109"),
                new BigDecimal("8.267"),
                new BigDecimal("34.451"),
                BigDecimal.valueOf(18),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(25),
                30, createdAt3, createdAt3);


        List<Violation> violations = List.of(v1, v2, v3, v4);

        when(violationRepository.findAllByDriverIdAndViolationDateAfter(eq(v1.getDriverId()), any()))
                .thenReturn(violations);

        DriverScore driverScore = driverScoringService.calculateScore(v1.getDriverId());
        assertEquals(0, driverScore.getCurrentScore());
        assertEquals("CRITICAL", driverScore.getScoreCategory());
    }
}