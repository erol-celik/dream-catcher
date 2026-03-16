package com.dreamdiary.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Bulk sync request payload from the mobile client.
 * Contains pending dreams and daily activities that were stored
 * locally while the device was offline.
 */
public record SyncRequest(
        @NotNull(message = "Dreams list is required")
        List<@Valid CreateDreamRequest> dreams,

        @NotNull(message = "Activities list is required")
        List<@Valid DailyActivityRequest> activities
) {
}
