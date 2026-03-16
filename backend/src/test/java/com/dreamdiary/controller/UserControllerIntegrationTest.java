package com.dreamdiary.controller;

import com.dreamdiary.BaseWebIntegrationTest;
import com.dreamdiary.dto.request.GuestRegistrationRequest;
import com.dreamdiary.enums.Platform;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIntegrationTest extends BaseWebIntegrationTest {

    @Test
    void registerGuest_ReturnsJwtToken_AndGrantsFreeTrial() throws Exception {
        GuestRegistrationRequest request = new GuestRegistrationRequest(
                "test-device-uuid-1",
                Platform.IOS,
                "1.0.0"
        );

        mockMvc.perform(post("/api/v1/users/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andExpect(jsonPath("$.guestToken").isNotEmpty())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.freeTrialAvailable").value(true));
    }

    @Test
    void registerGuest_WithSameDevice_DeniesFreeTrial() throws Exception {
        GuestRegistrationRequest request = new GuestRegistrationRequest(
                "test-device-uuid-2",
                Platform.ANDROID,
                "1.0.0"
        );

        // First registration gets trial
        mockMvc.perform(post("/api/v1/users/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.freeTrialAvailable").value(true));

        // Second registration on same device fingerprint is denied trial
        mockMvc.perform(post("/api/v1/users/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.freeTrialAvailable").value(false));
    }
}
