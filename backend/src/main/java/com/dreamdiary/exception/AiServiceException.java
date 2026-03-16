package com.dreamdiary.exception;

/**
 * Exception thrown when the AI provider fails to respond,
 * or when the response cannot be parsed.
 */
public class AiServiceException extends RuntimeException {

    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
