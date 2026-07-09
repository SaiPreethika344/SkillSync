package com.skillsync.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillsync.backend.dto.AnalysisResultDto;
import com.skillsync.backend.dto.CareerMatchDto;
import com.skillsync.backend.dto.DashboardCareerMatchDto;
import com.skillsync.backend.dto.DashboardResponseDto;
import com.skillsync.backend.dto.SkillStrengthDto;
import com.skillsync.backend.model.Analysis;
import com.skillsync.backend.model.User;
import com.skillsync.backend.repository.AnalysisRepository;
import com.skillsync.backend.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final CareerMappingService careerMappingService;
    private final ObjectMapper objectMapper;

    public DashboardService(
            AnalysisRepository analysisRepository,
            UserRepository userRepository,
            CareerMappingService careerMappingService,
            ObjectMapper objectMapper) {
        this.analysisRepository = analysisRepository;
        this.userRepository = userRepository;
        this.careerMappingService = careerMappingService;
        this.objectMapper = objectMapper;
    }

    public DashboardResponseDto getDashboard(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Analysis latest = analysisRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("No analysis found for user"));

        AnalysisResultDto result = readResult(latest.getResultJson());
        List<String> userSkills = readSkills(latest.getSkillsJson());

        List<CareerMatchDto> topMatchesRaw = result.getCareerMatches() == null
                ? List.of()
                : result.getCareerMatches().stream().limit(4).toList();
        List<DashboardCareerMatchDto> topCareerMatches = topMatchesRaw.stream()
                .map(match -> new DashboardCareerMatchDto(
                        match.getCareerTitle(),
                        match.getMatchPercentage(),
                        match.getDescription()))
                .toList();

        int topScore = topMatchesRaw.isEmpty() ? 0 : topMatchesRaw.getFirst().getMatchPercentage();
        String topCareer = topMatchesRaw.isEmpty() ? null : topMatchesRaw.getFirst().getCareerTitle();
        List<SkillStrengthDto> strengths = calculateSkillStrengths(userSkills, topCareer);

        DashboardResponseDto dto = new DashboardResponseDto();
        dto.setUserName(user.getName());
        dto.setTopCareerMatchScore(topScore);
        dto.setSkillStrengths(strengths);
        dto.setTopCareerMatches(topCareerMatches);
        return dto;
    }

    private List<SkillStrengthDto> calculateSkillStrengths(List<String> userSkills, String topCareer) {
        if (userSkills == null || userSkills.isEmpty()) {
            return List.of();
        }

        Set<String> topCareerSkills = topCareer == null
                ? Set.of()
                : careerMappingService.getSkillsForCareer(topCareer).stream()
                        .map(skill -> skill.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toSet());

        List<SkillStrengthDto> strengths = new ArrayList<>();
        for (String skill : userSkills) {
            if (skill == null || skill.isBlank()) {
                continue;
            }

            String normalized = skill.toLowerCase(Locale.ROOT).trim();
            int percentage;
            if (topCareerSkills.contains(normalized)) {
                percentage = 95;
            } else if (careerMappingService.isKnownSkill(skill)) {
                percentage = 75;
            } else {
                percentage = 60;
            }
            strengths.add(new SkillStrengthDto(skill.trim(), percentage));
        }

        strengths.sort((a, b) -> Integer.compare(b.getPercentage(), a.getPercentage()));
        return strengths;
    }

    private AnalysisResultDto readResult(String value) {
        try {
            return objectMapper.readValue(value, AnalysisResultDto.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse analysis result");
        }
    }

    private List<String> readSkills(String value) {
        try {
            return objectMapper.readValue(value, new TypeReference<List<String>>() {
            });
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse skills list");
        }
    }
}
