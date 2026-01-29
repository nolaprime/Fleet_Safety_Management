package com.fleet.scoring.repository;

import com.fleet.scoring.model.Violation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ViolationRepository extends JpaRepository<Violation, UUID> {
}
