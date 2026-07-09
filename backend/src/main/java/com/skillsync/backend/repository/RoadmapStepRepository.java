package com.skillsync.backend.repository;

import com.skillsync.backend.model.RoadmapStep;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapStepRepository extends JpaRepository<RoadmapStep, Long> {
    List<RoadmapStep> findByUserIdOrderByStepOrderAsc(Long userId);

    Optional<RoadmapStep> findByIdAndUserId(Long id, Long userId);

    void deleteByUserId(Long userId);
}
