package com.dreamdiary.ai;

import java.util.List;

/**
 * Result of the AI analysis on a single dream.
 * Extracted tags for weekly summary, and general sentiment.
 */
public record DreamAnalysisResult(
        List<String> tags,
        String sentiment
) {}
