package com.dreamcatcher.dto.response;

import com.dreamcatcher.enums.ActivityType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response payload for a daily activity entry.
 */
public record DailyActivityResponse(
        Long id,
        String clientId,
        LocalDate activityDate,
        ActivityType activityType,
        Long dreamId,
        String goalText,
        LocalDateTime createdAt
) {
}
