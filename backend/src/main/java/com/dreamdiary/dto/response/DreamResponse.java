package com.dreamdiary.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload for a dream entry, including AI-extracted tags and sentiment.
 */
public record DreamResponse(
        Long id,
        String clientId,
        String content,
        int wordCount,
        boolean valid,
        LocalDate dreamDate,
        List<String> tags,
        String sentiment,
        LocalDateTime createdAt
) {
}
