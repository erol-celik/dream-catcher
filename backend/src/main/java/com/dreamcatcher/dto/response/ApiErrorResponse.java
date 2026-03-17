package com.dreamcatcher.dto.response;

import java.time.LocalDateTime;

/**
 * Standard error response payload for all API errors.
 * Returned by GlobalExceptionHandler for consistent error formatting.
 */
public record ApiErrorResponse(
        int status,
        String errorCode,
        String message,
        LocalDateTime timestamp
) {
}
