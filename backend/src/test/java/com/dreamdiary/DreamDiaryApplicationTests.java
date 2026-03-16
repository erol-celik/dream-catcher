package com.dreamdiary;

import com.dreamdiary.ai.AiClient;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Basic integration test verifying that the Spring context
 * loads correctly with all entities and repositories.
 */
class DreamDiaryApplicationTests extends BaseIntegrationTest {

    @MockitoBean
    private AiClient aiClient;

    @Test
    void contextLoads() {
        // Verifies that all entity mappings, repository wiring,
        // and auto-configurations are valid.
    }

}
