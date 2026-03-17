package com.dreamcatcher.dto.request;

import com.dreamcatcher.enums.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for registering a new guest user.
 * Device fingerprint is required for free trial abuse prevention.
 */
public record GuestRegistrationRequest(
        @NotBlank(message = "Device fingerprint is required")
        String deviceFingerprint,

        @NotNull(message = "Platform is required")
        Platform platform,

        String appVersion
) {
}
