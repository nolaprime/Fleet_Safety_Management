package com.fleet.scoring.repository;

import com.fleet.scoring.model.Trips;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trips, String> {
}
