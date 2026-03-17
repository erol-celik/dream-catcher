package com.dreamcatcher.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when dream text fails client-side heuristic validation.
 * Examples: text is too short (<10 words) or is detected as gibberish.
 * Maps to HTTP 422 Unprocessable Entity.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class DreamValidationException extends RuntimeException {

    private final String validationCode;

    public DreamValidationException(String message, String validationCode) {
        super(message);
        this.validationCode = validationCode;
    }

    public String getValidationCode() {
        return validationCode;
    }

}
