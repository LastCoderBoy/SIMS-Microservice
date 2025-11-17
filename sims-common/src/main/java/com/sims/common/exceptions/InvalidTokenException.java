package com.sims.common.exceptions;

import java.io.Serial;

/**
 * Exception thrown when JWT token is invalid, expired, or malformed
 *
 * @author LastCoderBoy
 * @since 2025-01-17
 */
public class InvalidTokenException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
