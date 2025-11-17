package com.sims.common.exceptions;

import java.io.Serial;

/**
 * Generic exception for service layer errors
 * Used when business logic operations fail
 *
 * @author LastCoderBoy
 * @since 2025-01-17
 */
public class ServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
