package com.skillsync.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillsync.backend.dto.AnalysisHistoryItemDto;
import com.skillsync.backend.dto.AnalysisResultDto;
import com.skillsync.backend.dto.AnalysisRunRequest;
import com.skillsync.backend.dto.CareerMatchDto;
import com.skillsync.backend.model.Analysis;
import com.skillsync.backend.model.RoadmapStep;
import com.skillsync.backend.model.User;
import com.skillsync.backend.repository.AnalysisRepository;
import com.skillsync.backend.repository.RoadmapStepRepository;
import com.skillsync.backend.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final RoadmapStepRepository roadmapStepRepository;
    private final CareerMappingService careerMappingService;
    private final ObjectMapper objectMapper;

    public AnalysisService(
            AnalysisRepository analysisRepository,
            UserRepository userRepository,
            RoadmapStepRepository roadmapStepRepository,
            CareerMappingService careerMappingService,
            ObjectMapper objectMapper) {
        this.analysisRepository = analysisRepository;
        this.userRepository = userRepository;
        this.roadmapStepRepository = roadmapStepRepository;
        this.careerMappingService = careerMappingService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AnalysisResultDto runAnalysis(AnalysisRunRequest request) {
        User user = getCurrentUser();
        List<String> cleanedSkills = request.getSkills().stream()
                .filter(skill -> skill != null && !skill.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        if (cleanedSkills.isEmpty()) {
            throw new IllegalArgumentException("At least one valid skill is required");
        }

        List<CareerMatchDto> matches = careerMappingService.calculateCareerMatches(cleanedSkills, request.getField());
        List<String> skillGaps = careerMappingService.identifySkillGaps(cleanedSkills, matches);
        List<String> roadmapSteps = careerMappingService.generateRoadmapSteps(skillGaps);

        AnalysisResultDto resultDto = new AnalysisResultDto();
        resultDto.setCareerMatches(matches);
        resultDto.setSkillGaps(skillGaps);
        resultDto.setRoadmapSteps(roadmapSteps);

        Analysis analysis = new Analysis();
        analysis.setUser(user);
        analysis.setField(request.getField().trim());
        analysis.setSkillsJson(writeJson(cleanedSkills));
        analysis.setResultJson(writeJson(resultDto));
        analysisRepository.save(analysis);

        roadmapStepRepository.deleteByUserId(user.getId());
        List<RoadmapStep> stepsToSave = new ArrayList<>();
        for (int i = 0; i < roadmapSteps.size(); i++) {
            RoadmapStep step = new RoadmapStep();
            step.setUser(user);
            step.setTitle(roadmapSteps.get(i));
            step.setComplete(false);
            step.setStepOrder(i + 1);
            stepsToSave.add(step);
        }
        if (!stepsToSave.isEmpty()) {
            roadmapStepRepository.saveAll(stepsToSave);
        }

        return resultDto;
    }

    public List<AnalysisHistoryItemDto> getHistory() {
        User user = getCurrentUser();
        List<Analysis> analyses = analysisRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<AnalysisHistoryItemDto> history = new ArrayList<>();

        for (Analysis analysis : analyses) {
            AnalysisHistoryItemDto item = new AnalysisHistoryItemDto();
            item.setId(analysis.getId());
            item.setField(analysis.getField());
            item.setCreatedAt(analysis.getCreatedAt());
            item.setSkills(readSkills(analysis.getSkillsJson()));
            item.setResult(readResult(analysis.getResultJson()));
            history.add(item);
        }

        return history;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize analysis data");
        }
    }

    private List<String> readSkills(String value) {
        try {
            return objectMapper.readValue(value, new TypeReference<List<String>>() {
            });
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse skills data");
        }
    }

    private AnalysisResultDto readResult(String value) {
        try {
            return objectMapper.readValue(value, AnalysisResultDto.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse analysis result data");
        }
    }
}
