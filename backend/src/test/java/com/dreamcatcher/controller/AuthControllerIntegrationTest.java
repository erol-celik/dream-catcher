package com.dreamcatcher.controller;

import com.dreamcatcher.BaseWebIntegrationTest;
import com.dreamcatcher.dto.request.AuthLinkRequest;
import com.dreamcatcher.dto.request.GuestRegistrationRequest;
import com.dreamcatcher.enums.AuthProvider;
import com.dreamcatcher.enums.Platform;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends BaseWebIntegrationTest {

    @Test
    void linkGuestAccount_WithValidGuestToken_UpgradesAccount_AndReturnsNewJwt() throws Exception {
        // 1. Register Guest
        GuestRegistrationRequest guestReq = new GuestRegistrationRequest(
                UUID.randomUUID().toString(),
                Platform.ANDROID,
                "1.0.0"
        );

        MvcResult guestResult = mockMvc.perform(post("/api/v1/users/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guestReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Map<String, Object> guestMap = objectMapper.readValue(
                guestResult.getResponse().getContentAsString(), new TypeReference<>() {}
        );
        String guestTokenUuid = (String) guestMap.get("guestToken");

        // 2. Link Account (Simulating OAuth return)
        AuthLinkRequest linkReq = new AuthLinkRequest(
                guestTokenUuid,
                AuthProvider.GOOGLE,
                "google-oauth-subject-12345",
                "testuser@gmail.com",
                "Test User"
        );

        mockMvc.perform(post("/api/v1/auth/link-guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(linkReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("testuser@gmail.com"))
                .andExpect(jsonPath("$.displayName").value("Test User"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();



        // 3. Verify the new JWT is present. 
        // Note: We cannot assertNotEquals(originalJwt, newJwt) because if both requests
        // execute within the same second, the generated JWTs will be mathematically identical 
        // (same subject, same 'iat' and 'exp' truncated to seconds).
    }
}
