package com.dreamcatcher.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request payload for creating a new dream entry.
 * The clientId is a UUID generated on the mobile client for idempotent sync.
 */
public record CreateDreamRequest(
        @NotBlank(message = "Client ID is required")
        @Size(max = 255)
        String clientId,

        @NotBlank(message = "Dream content is required")
        @Size(max = 5000, message = "Dream content is too long. Max 5000 characters allowed.")
        String content,

        @NotNull(message = "Dream date is required")
        LocalDate dreamDate
) {
}
