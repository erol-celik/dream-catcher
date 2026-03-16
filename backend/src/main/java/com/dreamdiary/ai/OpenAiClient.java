package com.dreamdiary.ai;

import com.dreamdiary.config.AiProperties;
import com.dreamdiary.exception.AiServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Implementation of AiClient using OpenAI's API.
 * Uses Spring's RestClient and Jackson for robust JSON parsing.
 */
@Component
@Slf4j
public class OpenAiClient implements AiClient {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    private final AiProperties aiProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public OpenAiClient(AiProperties aiProperties, ObjectMapper objectMapper) {
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(OPENAI_API_URL)
                .defaultHeader("Authorization", "Bearer " + aiProperties.getApiKey())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public DreamAnalysisResult analyzeDream(String dreamText) {
        if (!"openai".equalsIgnoreCase(aiProperties.getProvider())) {
            log.warn("AiClient called but provider is set to: {}", aiProperties.getProvider());
            // Safe fallback to prevent breaking UI if external API is down or misconfigured
            return new DreamAnalysisResult(List.of("dream", "sleep"), "NEUTRAL");
        }

        Map<String, Object> requestBody = Map.of(
                "model", aiProperties.getModel(),
                "response_format", Map.of("type", "json_object"), // Enforce JSON output
                "messages", List.of(
                        Map.of("role", "system", "content", PromptConstants.DAILY_DREAM_ANALYSIS_SYSTEM_PROMPT),
                        Map.of("role", "user", "content", "Dream text: " + dreamText)
                )
        );

        String jsonResponse = callApi(requestBody);
        return parseResponse(jsonResponse, DreamAnalysisResult.class);
    }

    @Override
    public WeeklyAnalysisResult generateWeeklyReport(String weeklyTagsJson) {
        if (!"openai".equalsIgnoreCase(aiProperties.getProvider())) {
            return new WeeklyAnalysisResult("Weekly Reflection", "A general overview of your current state of mind.");
        }

        Map<String, Object> requestBody = Map.of(
                "model", aiProperties.getModel(),
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", PromptConstants.WEEKLY_REPORT_SYSTEM_PROMPT),
                        Map.of("role", "user", "content", "Recent tags: " + weeklyTagsJson)
                )
        );

        String jsonResponse = callApi(requestBody);
        return parseResponse(jsonResponse, WeeklyAnalysisResult.class);
    }

    private String callApi(Map<String, Object> requestBody) {
        try {
            String requestJson = objectMapper.writeValueAsString(requestBody);

            log.debug("Calling OpenAI API with model: {}", aiProperties.getModel());
            Map<String, Object> response = restClient.post()
                    .body(requestJson)
                    .retrieve()
                    // If 4xx or 5xx, RestClient throws RestClientResponseException
                    .body(Map.class);

            if (response == null || !response.containsKey("choices")) {
                throw new AiServiceException("Invalid response format from OpenAI");
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices.isEmpty()) {
                throw new AiServiceException("OpenAI returned no choices");
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            log.error("Failed to connect to OpenAI API: {}", e.getMessage());
            throw new AiServiceException("AI request failed", e);
        }
    }

    private <T> T parseResponse(String json, Class<T> targetClass) {
        try {
            return objectMapper.readValue(json, targetClass);
        } catch (Exception e) {
            log.error("Failed to parse AI JSON response: {}", json);
            throw new AiServiceException("Failed to parse AI response into " + targetClass.getSimpleName(), e);
        }
    }
}
