package com.dreamcatcher;

import com.dreamcatcher.ai.AiClient;
import com.dreamcatcher.ai.DreamAnalysisResult;
import com.dreamcatcher.ai.WeeklyAnalysisResult;
import com.dreamcatcher.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Base abstract class for Web Integration tests.
 * Configures MockMvc, ObjectMapper, and Mocks the external AiClient to avoid
 * hitting the real OpenAI API during automated testing.
 */
@AutoConfigureMockMvc
public abstract class BaseWebIntegrationTest extends BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtUtil jwtUtil;

    // Mock the external AI service to avoid real billing / network calls
    // Using MockitoBean for Spring Boot 3.4+ compatibility
    @MockitoBean
    protected AiClient aiClient;

    @BeforeEach
    void setupAiMock() {
        // Mock Dream Analysis
        DreamAnalysisResult dummyAnalysis = new DreamAnalysisResult(
                List.of("test-tag-1", "test-tag-2"),
                "POSITIVE"
        );
        when(aiClient.analyzeDream(anyString()))
                .thenReturn(dummyAnalysis);

        // Mock Weekly Report
        WeeklyAnalysisResult dummyWeekly = new WeeklyAnalysisResult(
                "Testing consistency",
                "This is a mock weekly report summary."
        );
        when(aiClient.generateWeeklyReport(any()))
                .thenReturn(dummyWeekly);
    }

    /**
     * Helper to retrieve a valid Bearer token for testing authenticated endpoints.
     */
    protected String getBearerToken(Long userId) {
        return "Bearer " + jwtUtil.generateToken(userId);
    }

}
