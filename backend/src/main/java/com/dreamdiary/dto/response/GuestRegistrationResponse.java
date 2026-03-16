package com.dreamdiary.dto.response;

/**
 * Response payload for guest user registration.
 * Includes whether the free trial is available based on device fingerprint check.
 */
public record GuestRegistrationResponse(
        Long userId,
        String guestToken,
        String token,
        boolean freeTrialAvailable
) {
}
