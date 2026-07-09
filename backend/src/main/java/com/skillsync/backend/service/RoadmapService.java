package com.skillsync.backend.service;

import com.skillsync.backend.dto.RoadmapStepDto;
import com.skillsync.backend.model.RoadmapStep;
import com.skillsync.backend.model.User;
import com.skillsync.backend.repository.RoadmapStepRepository;
import com.skillsync.backend.repository.UserRepository;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoadmapService {

    private final RoadmapStepRepository roadmapStepRepository;
    private final UserRepository userRepository;

    public RoadmapService(RoadmapStepRepository roadmapStepRepository, UserRepository userRepository) {
        this.roadmapStepRepository = roadmapStepRepository;
        this.userRepository = userRepository;
    }

    public List<RoadmapStepDto> getRoadmapForCurrentUser() {
        User user = getCurrentUser();
        return roadmapStepRepository.findByUserIdOrderByStepOrderAsc(user.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public RoadmapStepDto markStepComplete(Long id) {
        User user = getCurrentUser();
        RoadmapStep step = roadmapStepRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Roadmap step not found"));
        step.setComplete(true);
        return toDto(step);
    }

    @Transactional
    public RoadmapStepDto undoStepComplete(Long id) {
        User user = getCurrentUser();
        RoadmapStep step = roadmapStepRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Roadmap step not found"));
        step.setComplete(false);
        return toDto(step);
    }

    private RoadmapStepDto toDto(RoadmapStep step) {
        RoadmapStepDto dto = new RoadmapStepDto();
        dto.setId(step.getId());
        dto.setTitle(step.getTitle());
        dto.setComplete(step.isComplete());
        dto.setStepOrder(step.getStepOrder());
        return dto;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
