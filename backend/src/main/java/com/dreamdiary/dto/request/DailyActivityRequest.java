package com.dreamdiary.dto.request;

import com.dreamdiary.enums.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request payload for logging a daily activity.
 * Used for all activity types: DREAM, NO_DREAM, and GOAL.
 * - For DREAM: dreamClientId should reference the dream entry.
 * - For NO_DREAM: no extra fields needed.
 * - For GOAL: goalText contains the user's answer to the daily question.
 */
public record DailyActivityRequest(
        @NotBlank(message = "Client ID is required")
        String clientId,

        @NotNull(message = "Activity date is required")
        LocalDate activityDate,

        @NotNull(message = "Activity type is required")
        ActivityType activityType,

        String dreamClientId,

        @Size(max = 500, message = "Goal text must not exceed 500 characters")
        String goalText
) {
}
