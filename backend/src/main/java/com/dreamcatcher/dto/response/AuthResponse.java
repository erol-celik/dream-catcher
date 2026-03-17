package com.dreamcatcher.dto.response;

import lombok.Builder;

/**
 * Payload returned after a successful authentication or account link.
 * Contains the generated JWT.
 */
@Builder
public record AuthResponse(
        String token,
        Long userId,
        String email,
        String displayName
) {
}
