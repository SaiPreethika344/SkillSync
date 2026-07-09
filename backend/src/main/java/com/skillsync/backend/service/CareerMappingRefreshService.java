package com.skillsync.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillsync.backend.model.CareerMapping;
import com.skillsync.backend.repository.CareerMappingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class CareerMappingRefreshService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final CareerMappingRepository careerMappingRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public CareerMappingRefreshService(CareerMappingRepository careerMappingRepository, ObjectMapper objectMapper) {
        this.careerMappingRepository = careerMappingRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Scheduled(cron = "0 0 6 * * MON")
    public void refreshCareerMappings() {
        System.out.println("Starting weekly career mapping refresh...");
        List<CareerMapping> careers = careerMappingRepository.findByIsActiveTrue();
        int currentYear = LocalDate.now().getYear();

        for (CareerMapping career : careers) {
            try {
                String prompt = "You are a job market analyst. Based on current industry trends in " + currentYear +
                        ", list the top 8 most in-demand skills for a " + career.getCareerTitle() +
                        ". Include both technical and soft skills. Return ONLY a JSON array of skill name strings, nothing else. Example: [\"Python\",\"SQL\",\"Communication\"]";

                Map<String, Object> payload = new HashMap<>();
                payload.put("model", "llama-3.1-8b-instant");
                payload.put("max_tokens", 200);
                payload.put("messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ));

                String requestBody = objectMapper.writeValueAsString(payload);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                        .header("Authorization", "Bearer " + groqApiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                JsonNode root = objectMapper.readTree(response.body());
                String content = root.path("choices").path(0).path("message").path("content").asText("");

                content = content.trim();
                if (content.startsWith("[") && content.endsWith("]")) {
                    JsonNode skillsArray = objectMapper.readTree(content);
                    StringBuilder skills = new StringBuilder();
                    for (JsonNode skill : skillsArray) {
                        if (skills.length() > 0) skills.append(",");
                        skills.append(skill.asText());
                    }
                    career.setRequiredSkills(skills.toString());
                    careerMappingRepository.save(career);
                    System.out.println("Updated " + career.getCareerTitle() + " → " + skills);
                }

                Thread.sleep(1000);

            } catch (Exception e) {
                System.err.println("Failed to refresh " + career.getCareerTitle() + ": " + e.getMessage());
            }
        }
        System.out.println("Weekly career mapping refresh completed.");
    }
}