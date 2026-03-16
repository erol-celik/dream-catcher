package com.dreamdiary.controller;

import com.dreamdiary.BaseWebIntegrationTest;
import com.dreamdiary.dto.request.CreateDreamRequest;
import com.dreamdiary.dto.request.DailyActivityRequest;
import com.dreamdiary.dto.request.SyncRequest;
import com.dreamdiary.entity.User;
import com.dreamdiary.enums.ActivityType;
import com.dreamdiary.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SyncControllerIntegrationTest extends BaseWebIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private String bearerToken;

    @BeforeEach
    void setupUser() {
        testUser = User.builder()
                .isGuest(true)
                .guestToken(UUID.randomUUID().toString())
                .build();
        testUser = userRepository.save(testUser);
        bearerToken = getBearerToken(testUser.getId());
    }

    @Test
    void syncData_WithValidItems_ProcessesSuccessfully() throws Exception {
        CreateDreamRequest dreamRequest = new CreateDreamRequest(
                UUID.randomUUID().toString(),
                "A vivid dream about testing the offline sync feature.",
                LocalDate.now()
        );

        DailyActivityRequest activityRequest = new DailyActivityRequest(
                UUID.randomUUID().toString(),
                LocalDate.now(),
                ActivityType.NO_DREAM,
                null,
                "Meditation"
        );

        SyncRequest syncRequest = new SyncRequest(
                List.of(dreamRequest),
                List.of(activityRequest)
        );

        mockMvc.perform(post("/api/v1/sync")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.syncedDreams").isArray())
                .andExpect(jsonPath("$.syncedDreams.length()").value(1))
                .andExpect(jsonPath("$.syncedDreams[0].success").value(true))
                .andExpect(jsonPath("$.syncedActivities").isArray())
                .andExpect(jsonPath("$.syncedActivities.length()").value(1))
                .andExpect(jsonPath("$.syncedActivities[0].success").value(true))
                .andExpect(jsonPath("$.totalSynced").value(2))
                .andExpect(jsonPath("$.totalFailed").value(0));
    }
}
