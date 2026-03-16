package com.dreamdiary.dto.response;

import com.dreamdiary.enums.ActivityType;

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
