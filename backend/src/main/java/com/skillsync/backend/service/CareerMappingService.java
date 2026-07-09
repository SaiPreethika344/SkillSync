package com.skillsync.backend.service;

import com.skillsync.backend.dto.CareerMatchDto;
import com.skillsync.backend.model.CareerMapping;
import com.skillsync.backend.repository.CareerMappingRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Service;

@Service
public class CareerMappingService {

    private final CareerMappingRepository careerMappingRepository;

    public CareerMappingService(CareerMappingRepository careerMappingRepository) {
        this.careerMappingRepository = careerMappingRepository;
    }

    public List<CareerMatchDto> calculateCareerMatches(List<String> rawSkills, String field) {
        Set<String> normalizedSkills = normalizeSkills(rawSkills);
        List<CareerMatchDto> matches = new ArrayList<>();
        Map<String, List<String>> careerMap = careerSkillMap();
        List<String> fieldKeywords = getFieldKeywords(field);

        for (Map.Entry<String, List<String>> entry : careerMap.entrySet()) {
            String career = entry.getKey();

            // Filter by field if provided
            if (field != null && !field.isBlank() && !fieldKeywords.isEmpty()) {
                boolean matchesField = false;
                String careerLower = career.toLowerCase(Locale.ROOT);
                for (String keyword : fieldKeywords) {
                    if (careerLower.contains(keyword.toLowerCase(Locale.ROOT))) {
                        matchesField = true;
                        break;
                    }
                }
                if (!matchesField) {
                    continue;
                }
            }

            List<String> careerSkills = entry.getValue();
            int matchedCount = 0;

            for (String requiredSkill : careerSkills) {
                if (normalizedSkills.contains(normalizeSkill(requiredSkill))) {
                    matchedCount++;
                }
            }

            double percent = (double) matchedCount / careerSkills.size() * 100.0;
            CareerMatchDto dto = new CareerMatchDto(career, (int) Math.round(percent));
            dto.setMissingSkills(getMissingSkillsForCareer(rawSkills, career));
            matches.add(dto);
        }

        matches.sort(Comparator.comparingInt(CareerMatchDto::getMatchPercentage).reversed());
        return matches;
    }

    private List<String> getFieldKeywords(String field) {
        if (field == null || field.isBlank()) {
            return List.of();
        }

        return switch (field.trim()) {
            case "Law & Social Sciences" -> List.of("Law", "Legal", "Policy", "Social", "Psychology", "Sociology");
            case "Engineering & Technology" -> List.of("Engineer", "Developer", "Data", "Software", "Tech");
            case "Medical & Health Sciences" -> List.of("Doctor", "Medical", "Health", "Nurse", "Clinical", "Researcher");
            case "Business & Commerce" -> List.of("Business", "Finance", "Analyst", "Manager", "Marketing", "Accountant");
            case "Arts, Design & Media" -> List.of("Designer", "Artist", "Media", "Content", "Creative", "Writer");
            case "Science & Research" -> List.of("Scientist", "Researcher", "Analyst", "Lab", "Science");
            default -> List.of();
        };
    }

    public List<String> identifySkillGaps(List<String> rawSkills, List<CareerMatchDto> careerMatches) {
        if (careerMatches.isEmpty()) {
            return List.of();
        }
        String topCareer = careerMatches.getFirst().getCareerTitle();
        List<String> requiredSkills = careerSkillMap().getOrDefault(topCareer, List.of());
        Set<String> normalizedSkills = normalizeSkills(rawSkills);

        List<String> gaps = new ArrayList<>();
        for (String requiredSkill : requiredSkills) {
            if (!normalizedSkills.contains(normalizeSkill(requiredSkill))) {
                gaps.add(requiredSkill);
            }
        }
        return gaps;
    }

    public List<String> generateRoadmapSteps(List<String> skillGaps) {
        List<String> steps = new ArrayList<>();
        for (String gap : skillGaps) {
            steps.add("Learn " + gap + " fundamentals");
            steps.add("Complete one project using " + gap);
        }
        return steps;
    }

    public List<String> getSkillsForCareer(String career) {
        return careerSkillMap().getOrDefault(career, List.of());
    }

    public boolean isKnownSkill(String skill) {
        String normalized = normalizeSkill(skill);
        return careerSkillMap().values().stream()
                .flatMap(List::stream)
                .map(this::normalizeSkill)
                .anyMatch(normalized::equals);
    }

    public List<String> getMissingSkillsForCareer(List<String> rawSkills, String career) {
        List<String> requiredSkills = careerSkillMap().getOrDefault(career, List.of());
        Set<String> normalizedSkills = normalizeSkills(rawSkills);
        List<String> missing = new ArrayList<>();
        for (String requiredSkill : requiredSkills) {
            if (!normalizedSkills.contains(normalizeSkill(requiredSkill))) {
                missing.add(requiredSkill);
            }
        }
        return missing;
    }

    public Set<String> getAllKnownSkills() {
        Set<String> skills = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        careerSkillMap().values().forEach(skills::addAll);
        return skills;
    }

    private Set<String> normalizeSkills(List<String> rawSkills) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String skill : rawSkills) {
            if (skill != null && !skill.isBlank()) {
                normalized.add(normalizeSkill(skill));
            }
        }
        return normalized;
    }

    private String normalizeSkill(String input) {
        return input.trim().toLowerCase(Locale.ROOT);
    }

    private Map<String, List<String>> careerSkillMap() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        List<CareerMapping> activeMappings = careerMappingRepository.findByIsActiveTrue();
        for (CareerMapping mapping : activeMappings) {
            List<String> skills = parseSkills(mapping.getRequiredSkills());
            map.put(mapping.getCareerTitle(), skills);
        }
        return map;
    }

    private List<String> parseSkills(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        String[] parts = csv.split(",");
        List<String> skills = new ArrayList<>();
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                skills.add(part.trim());
            }
        }
        return skills;
    }
}
