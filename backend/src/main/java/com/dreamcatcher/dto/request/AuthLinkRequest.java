package com.dreamcatcher.dto.request;

import com.dreamcatcher.enums.AuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Payload for authenticating and linking a guest account to a permanent OAuth profile.
 */
public record AuthLinkRequest(
        @NotBlank(message = "Guest token is required")
        String guestToken,

        @NotNull(message = "Auth provider is required")
        AuthProvider authProvider,

        @NotBlank(message = "Provider ID is required")
        String providerId,

        @NotBlank(message = "Email is required")
        String email,

        String displayName
) {
}
