package com.fleet.scoring.repository;

import com.fleet.scoring.model.DriverScore;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverScoreRepository extends JpaRepository<DriverScore, String> {
    DriverScore findByDriverId(String driverId);

    List<DriverScore> findByOrderByCurrentScoreDesc(Limit of);

    List<DriverScore> findByOrderByCurrentScoreAsc(Limit of);
}
