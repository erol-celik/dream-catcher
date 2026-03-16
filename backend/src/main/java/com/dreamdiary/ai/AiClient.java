package com.dreamdiary.ai;

/**
 * Interface representing the generic AI client.
 * Allows switching between OpenAI, Gemini, or other providers.
 */
public interface AiClient {

    /**
     * SENDS a request to the AI model to analyze a single dream.
     * 
     * @param dreamText The raw text of the dream.
     * @return The parsed tags and sentiment.
     */
    DreamAnalysisResult analyzeDream(String dreamText);

    /**
     * SENDS a request to the AI model to generate a weekly report
     * based on the tags from the week's dreams.
     * 
     * @param weeklyTagsJson A JSON-formatted string of the aggregated tags.
     * @return The parsed core theme and summary.
     */
    WeeklyAnalysisResult generateWeeklyReport(String weeklyTagsJson);

}
