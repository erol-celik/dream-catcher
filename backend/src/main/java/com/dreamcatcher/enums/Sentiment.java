package com.dreamcatcher.enums;

import lombok.extern.slf4j.Slf4j;

/**
 * Sentiment categories for dreams, covering both AI-extracted
 * and client-provided values.
 * NIGHTMARE and GREAT are user-facing sentiments selected on the mobile client.
 * POSITIVE, NEGATIVE, NEUTRAL, MIXED are AI-generated categories.
 */
@Slf4j
public enum Sentiment {
    POSITIVE,
    NEGATIVE,
    NEUTRAL,
    MIXED,
    NIGHTMARE,
    GREAT;

    /**
     * Safely parses a string into a Sentiment enum value.
     * Returns NEUTRAL for null, blank, or unrecognised inputs
     * instead of throwing an exception.
     */
    public static Sentiment fromString(String value) {
        if (value == null || value.isBlank()) {
            return NEUTRAL;
        }
        try {
            return Sentiment.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown sentiment value received: '{}', defaulting to NEUTRAL", value);
            return NEUTRAL;
        }
    }
}
