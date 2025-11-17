package com.sims.common.exceptions;

import java.io.Serial;

/**
 * Exception thrown when database operations fail
 * Wraps Spring DataAccessException for cleaner error handling
 *
 * @author LastCoderBoy
 * @since 2025-01-17
 */
public class DatabaseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
