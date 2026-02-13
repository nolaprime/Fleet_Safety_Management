package com.fleet.scoring.repository;

import com.fleet.scoring.model.Violation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ViolationRepository extends JpaRepository<Violation, UUID> {
    List<Violation> findAll(String driverId);
    List<Violation> findAllByDriverIdAndViolationDateAfter(String driverId, LocalDateTime startDate);
}
