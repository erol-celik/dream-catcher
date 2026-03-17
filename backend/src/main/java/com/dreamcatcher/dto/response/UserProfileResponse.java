package com.dreamcatcher.dto.response;

import java.time.LocalDateTime;

/**
 * Response payload for the user's profile information.
 */
public record UserProfileResponse(
        Long id,
        String email,
        String displayName,
        boolean guest,
        boolean premium,
        LocalDateTime premiumExpiresAt,
        int totalDreams,
        int currentStreak
) {
}
