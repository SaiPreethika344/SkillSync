package com.skillsync.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillsync.backend.dto.JobListingDto;
import com.skillsync.backend.model.Analysis;
import com.skillsync.backend.model.User;
import com.skillsync.backend.repository.AnalysisRepository;
import com.skillsync.backend.repository.UserRepository;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class JobsService {

    @Value("${adzuna.app.id}")
    private String adzunaAppId;

    @Value("${adzuna.app.key}")
    private String adzunaAppKey;

    private final UserRepository userRepository;
    private final AnalysisRepository analysisRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public JobsService(UserRepository userRepository, AnalysisRepository analysisRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.analysisRepository = analysisRepository;
        this.objectMapper = objectMapper;
    }

    public List<JobListingDto> getLiveJobsForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Analysis latestAnalysis = analysisRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("No analysis found for this user"));

        List<String> skills = readSkills(latestAnalysis.getSkillsJson());
        if (skills.isEmpty()) {
            throw new IllegalArgumentException("No skills found in latest analysis");
        }

        String searchTerms = skills.stream()
                .filter(skill -> skill != null && !skill.isBlank())
                .map(String::trim)
                .limit(3)
                .collect(Collectors.joining(" "));

        if (searchTerms.isBlank()) {
            throw new IllegalArgumentException("No valid skills found in latest analysis");
        }

        String url = UriComponentsBuilder.fromHttpUrl("https://api.adzuna.com/v1/api/jobs/in/search/1")
                .queryParam("app_id", adzunaAppId)
                .queryParam("app_key", adzunaAppKey)
                .queryParam("results_per_page", 10)
                .queryParam("what", searchTerms)
                .toUriString();

        ResponseEntity<String> response;
        try {
            response = restTemplate.getForEntity(url, String.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to fetch jobs from Adzuna");
        }

        return parseAdzunaResults(response.getBody());
    }

    private List<JobListingDto> parseAdzunaResults(String responseBody) {
        List<JobListingDto> jobs = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode results = root.path("results");
            if (!results.isArray()) {
                return jobs;
            }

            for (JsonNode item : results) {
                String title = item.path("title").asText("N/A");
                String company = item.path("company").path("display_name").asText("N/A");
                String location = item.path("location").path("display_name").asText("N/A");
                String salary = formatSalary(item.path("salary_min"), item.path("salary_max"));
                jobs.add(new JobListingDto(title, company, salary, location));
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse Adzuna response");
        }
        return jobs;
    }

    private String formatSalary(JsonNode minNode, JsonNode maxNode) {
        boolean hasMin = !minNode.isMissingNode() && !minNode.isNull();
        boolean hasMax = !maxNode.isMissingNode() && !maxNode.isNull();
        if (!hasMin && !hasMax) {
            return "Not disclosed";
        }

        DecimalFormat formatter = new DecimalFormat("#,###");
        if (hasMin && hasMax) {
            return formatter.format(minNode.asDouble()) + " - " + formatter.format(maxNode.asDouble());
        }
        if (hasMin) {
            return "From " + formatter.format(minNode.asDouble());
        }
        return "Up to " + formatter.format(maxNode.asDouble());
    }

    private List<String> readSkills(String skillsJson) {
        try {
            return objectMapper.readValue(skillsJson, new TypeReference<List<String>>() {
            });
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse latest analysis skills");
        }
    }
}
