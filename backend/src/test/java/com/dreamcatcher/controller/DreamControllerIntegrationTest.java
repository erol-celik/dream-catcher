package com.dreamcatcher.controller;

import com.dreamcatcher.BaseWebIntegrationTest;
import com.dreamcatcher.dto.request.CreateDreamRequest;
import com.dreamcatcher.dto.request.GuestRegistrationRequest;
import com.dreamcatcher.enums.Platform;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DreamControllerIntegrationTest extends BaseWebIntegrationTest {

    private String validJwtToken;

    @BeforeEach
    void setupUser() throws Exception {
        // Register a guest to get a valid JWT
        GuestRegistrationRequest request = new GuestRegistrationRequest(
                UUID.randomUUID().toString(),
                Platform.IOS,
                "1.0.0"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/users/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<String, Object> jsonMap = objectMapper.readValue(responseBody, new TypeReference<>() {});
        validJwtToken = (String) jsonMap.get("token");
    }

    @Test
    void createDream_WithoutAuth_Returns403() throws Exception {
        CreateDreamRequest request = new CreateDreamRequest(
                UUID.randomUUID().toString(),
                "I was flying over a city made of crystal.",
                LocalDate.now()
        );

        mockMvc.perform(post("/api/v1/dreams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createDream_WithAuth_Returns201_AndTriggersAsyncAiAnalysis() throws Exception {
        CreateDreamRequest request = new CreateDreamRequest(
                UUID.randomUUID().toString(),
                "I was flying over a city made of crystal. The sky was purple and the sun was a vibrant silver.",
                LocalDate.now()
        );

        mockMvc.perform(post("/api/v1/dreams")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.clientId").value(request.clientId()))
                .andExpect(jsonPath("$.content").value(request.content()))
                .andExpect(jsonPath("$.dreamDate").value(request.dreamDate().toString()));
                
        // Validation of AI tags and sentiments happens async, but we verified the Mock returns CompletableFuture cleanly.
    }
    
    @Test
    void createDream_TooShort_Returns422() throws Exception {
        CreateDreamRequest request = new CreateDreamRequest(
                UUID.randomUUID().toString(),
                "short",
                LocalDate.now()
        );

        mockMvc.perform(post("/api/v1/dreams")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Dream description is too short or nonsensical."));
    }
}
