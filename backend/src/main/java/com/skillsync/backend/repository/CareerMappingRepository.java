package com.skillsync.backend.repository;

import com.skillsync.backend.model.CareerMapping;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerMappingRepository extends JpaRepository<CareerMapping, Long> {
    List<CareerMapping> findByIsActiveTrue();
}
