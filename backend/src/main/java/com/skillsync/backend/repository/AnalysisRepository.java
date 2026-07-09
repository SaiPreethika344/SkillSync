package com.skillsync.backend.repository;

import com.skillsync.backend.model.Analysis;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    List<Analysis> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Analysis> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
}
