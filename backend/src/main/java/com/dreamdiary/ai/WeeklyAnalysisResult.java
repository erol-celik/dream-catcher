package com.dreamdiary.ai;

/**
 * Result of the AI weekly analysis generated from 7 dreams' tags.
 */
public record WeeklyAnalysisResult(
        String coreTheme,
        String summary
) {}
