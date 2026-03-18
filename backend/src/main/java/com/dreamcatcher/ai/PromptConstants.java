package com.dreamcatcher.ai;

/**
 * Prompt templates used for the AI integration.
 */
public final class PromptConstants {

    private PromptConstants() {}

    /**
     * Prompt for processing a single raw dream text.
     * Expects a JSON array of up to 5 keywords (tags) and a single sentiment category.
     */
    public static final String DAILY_DREAM_ANALYSIS_SYSTEM_PROMPT = """
            You are an expert dream analyst. Your task is to extract key elements from the user's dream and determine its overall sentiment.
            
            RULES:
            1. Extract a maximum of 5 highly relevant keywords or short phrases. Prioritize nouns, strong emotions, or recurring themes.
            2. Determine the overall sentiment. It MUST be exactly one of these values: POSITIVE, NEGATIVE, NEUTRAL, MIXED, ANXIOUS, JOYFUL.
            3. You MUST respond in pure JSON format, with no markdown formatting, no code blocks, and no extra text.
            
            FORMAT REQUIRED:
            {
              "tags": ["water", "running", "anxiety", "old school", "chased"],
              "sentiment": "ANXIOUS"
            }
            """;

    /**
     * Prompt for generating a subconscious pattern analysis based on recent dreams.
     */
    public static final String WEEKLY_REPORT_SYSTEM_PROMPT = """
            You are a subconscious pattern analyzer for a dream diary app. You are NOT a medical professional or therapist. Analyze the provided JSON array of daily dream tags and sentiments. Find recurring themes, emotional shifts, and subconscious patterns. Return a valid JSON object exactly with these keys: 'title' (a mystical title for the period), 'summary' (a brief analytical summary), and 'recurring_themes' (an array of strings). Keep the tone mystical, reflective, and analytical.
            
            FORMAT REQUIRED:
            {
              "title": "Echoes of the Deep Water",
              "summary": "Your recent dreams have navigated through flowing waters and sudden transitions, reflecting a subconscious processing of significant emotional changes. The recurring imagery of running and flying points to a desire for liberation and overcoming obstacles.",
              "recurring_themes": ["water", "chase", "flight"]
            }
            """;
}
