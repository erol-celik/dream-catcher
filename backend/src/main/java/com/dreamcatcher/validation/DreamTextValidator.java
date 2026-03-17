package com.dreamcatcher.validation;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Heuristic validator for dream text content.
 * Performs client-side validation BEFORE sending text to the LLM API
 * to avoid wasting tokens on invalid or gibberish input.
 *
 * Validation rules:
 * 1. Must be at least MIN_WORD_COUNT words.
 * 2. Must not contain excessive character repetition (e.g., "aaaaaa").
 * 3. Must not match known gibberish patterns (e.g., "asdasd", "qwerty").
 */
@Component
public class DreamTextValidator {

    private static final int MIN_WORD_COUNT = 10;
    private static final double MAX_UNIQUE_CHAR_RATIO_THRESHOLD = 0.15;

    // Detects repeated character sequences like "aaaa", "ababab", "asdasd"
    private static final Pattern REPETITIVE_PATTERN =
            Pattern.compile("(.)\\1{4,}|(.{1,3})\\2{3,}", Pattern.CASE_INSENSITIVE);

    // Common keyboard mashing patterns
    private static final Pattern KEYBOARD_PATTERN =
            Pattern.compile("(?i)(qwerty|asdf|zxcv|qazwsx|wasd|hjkl){2,}");

    /**
     * Validates dream text and returns the result.
     *
     * @param text The raw dream text content.
     * @return A ValidationResult indicating pass/fail with a reason code.
     */
    public ValidationResult validate(String text) {
        if (text == null || text.isBlank()) {
            return ValidationResult.fail("EMPTY_TEXT", "Dream content cannot be empty.");
        }

        String trimmed = text.trim();
        String[] words = trimmed.split("\\s+");

        // Rule 1: Minimum word count
        if (words.length < MIN_WORD_COUNT) {
            return ValidationResult.fail("TOO_SHORT",
                    String.format("Dream must be at least %d words. Current: %d words.",
                            MIN_WORD_COUNT, words.length));
        }

        // Rule 2: Character diversity check (detects "aaaaaa bbbbb ccccc" type input)
        String lettersOnly = trimmed.replaceAll("[^a-zA-ZğüşıöçĞÜŞİÖÇ]", "").toLowerCase();
        if (!lettersOnly.isEmpty()) {
            long uniqueChars = lettersOnly.chars().distinct().count();
            double ratio = (double) uniqueChars / lettersOnly.length();
            if (ratio < MAX_UNIQUE_CHAR_RATIO_THRESHOLD) {
                return ValidationResult.fail("LOW_DIVERSITY",
                        "Dream content appears to be repetitive or invalid text.");
            }
        }

        // Rule 3: Repetitive pattern detection
        if (REPETITIVE_PATTERN.matcher(trimmed).find()) {
            return ValidationResult.fail("REPETITIVE_PATTERN",
                    "Dream content contains repetitive character patterns.");
        }

        // Rule 4: Keyboard mashing detection
        if (KEYBOARD_PATTERN.matcher(trimmed).find()) {
            return ValidationResult.fail("KEYBOARD_MASHING",
                    "Dream content appears to be random keyboard input.");
        }

        return ValidationResult.pass(words.length);
    }

    /**
     * Immutable result of dream text validation.
     */
    public record ValidationResult(
            boolean valid,
            int wordCount,
            String errorCode,
            String errorMessage
    ) {
        public static ValidationResult pass(int wordCount) {
            return new ValidationResult(true, wordCount, null, null);
        }

        public static ValidationResult fail(String errorCode, String errorMessage) {
            return new ValidationResult(false, 0, errorCode, errorMessage);
        }
    }

}
