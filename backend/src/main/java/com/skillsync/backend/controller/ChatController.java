package com.skillsync.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@PreAuthorize("isAuthenticated()")
@Validated
public class ChatController {

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String SYSTEM_PROMPT = "You are a career guidance assistant inside SkillSync AI. "
            + "You have access to the user's career roadmap and analysis. Be encouraging, specific and concise. "
            + "If the user says they are tired, tell them to rest and come back. Keep responses under 100 words.";

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public ChatController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request == null || isBlank(request.getMessage()) || isBlank(request.getContext())) {
            return ResponseEntity.badRequest()
                    .body(new ChatResponse("Both message and context are required."));
        }

        // Guard: GROQ_API_KEY not configured in this server session
        if (isBlank(groqApiKey)) {
            return ResponseEntity.ok(new ChatResponse(
                    "The AI guide is unavailable right now — the server's GROQ_API_KEY is not set. "
                    + "Please contact the administrator or restart the backend with: "
                    + "$env:GROQ_API_KEY = \"gsk_...\" then restart."));
        }

        String userContent = request.getMessage() + " Context: " + request.getContext();
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "openai/gpt-oss-20b");
        payload.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", userContent)));
        payload.put("max_tokens", 200);

        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            return ResponseEntity.ok(new ChatResponse("Failed to build request to AI service."));
        }

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_URL))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + groqApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();
            System.out.println("Groq status: " + response.statusCode());
            System.out.println("Groq response: " + responseBody);

            if (response.statusCode() == 401) {
                return ResponseEntity.ok(new ChatResponse(
                        "AI service authentication failed — check that GROQ_API_KEY is valid."));
            }

            JsonNode root = objectMapper.readTree(responseBody);

            // Check for Groq API-level error (e.g. rate limit, invalid model)
            JsonNode errorNode = root.path("error");
            if (!errorNode.isMissingNode()) {
                String errMsg = errorNode.path("message").asText("Unknown Groq error");
                System.err.println("Groq API error: " + errMsg);
                return ResponseEntity.ok(new ChatResponse(
                        "AI service error: " + errMsg));
            }

            String reply = root.path("choices").path(0).path("message").path("content").asText("");
            if (reply.isBlank()) {
                return ResponseEntity.ok(new ChatResponse(
                        "The AI returned an empty response. Please try again."));
            }
            return ResponseEntity.ok(new ChatResponse(reply));
        } catch (Exception ex) {
            System.err.println("Groq error: " + ex.getMessage());
            return ResponseEntity.ok(new ChatResponse(
                    "Could not reach the AI service. Check server connectivity."));
        }
    }

    // NOTE: Both handlers return JSON ChatResponse, not plain text.
    // Plain-text error bodies caused Android JSONException → "Error parsing server response".
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ChatResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ChatResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ChatResponse> handleStateError(IllegalStateException ex) {
        return ResponseEntity.ok().body(new ChatResponse(ex.getMessage()));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static class ChatRequest {
        @NotBlank
        private String message;

        @NotBlank
        private String context;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getContext() {
            return context;
        }

        public void setContext(String context) {
            this.context = context;
        }
    }

    public static class ChatResponse {
        private String reply;

        public ChatResponse(String reply) {
            this.reply = reply;
        }

        public String getReply() {
            return reply;
        }

        public void setReply(String reply) {
            this.reply = reply;
        }
    }
}
