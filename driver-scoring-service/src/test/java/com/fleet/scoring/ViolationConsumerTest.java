package com.fleet.scoring;

import com.fleet.scoring.model.Driver;
import com.fleet.scoring.model.Violation;
import com.fleet.scoring.repository.DriverRepository;
import com.fleet.scoring.repository.ViolationRepository;
import com.fleet.scoring.service.DriverScoringService;
import com.fleet.scoring.service.ViolationConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class ViolationConsumerTest {

    @Autowired
    private ViolationRepository violationRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private DriverScoringService driverScoringService;

    @Test
    void testConcurrencyGracefully () {
            String driverId = "DRV-001";
            Timestamp ts = new Timestamp(System.currentTimeMillis());

            // 1. Manually insert a driver to simulate "another thread" beating us to it
            Driver existing = new Driver();
            existing.setId(driverId);
            driverRepository.saveAndFlush(existing);

            // 2. Call the method. It will see the driver exists OR
            // if we forced a race, saveAndFlush would throw an error we catch.
            assertDoesNotThrow(() -> {
                driverScoringService.ensureDriverExists(driverId, ts);
            });

            // 3. Verify we still only have 1 driver (no duplicates)
            assertEquals(1, driverRepository.count());
        }
}
