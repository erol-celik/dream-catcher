package com.dreamcatcher.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Result of the AI subconscious pattern analysis generated from recent dreams.
 */
public record WeeklyAnalysisResult(
        String title,
        String summary,
        @JsonProperty("recurring_themes") List<String> recurringThemes
) {}
