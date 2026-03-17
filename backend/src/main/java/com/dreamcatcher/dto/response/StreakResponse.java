package com.dreamcatcher.dto.response;

import java.time.LocalDate;

/**
 * Response payload for the user's streak information.
 */
public record StreakResponse(
        int currentStreak,
        int longestStreak,
        LocalDate lastActivityDate,
        boolean activeToday
) {
}
