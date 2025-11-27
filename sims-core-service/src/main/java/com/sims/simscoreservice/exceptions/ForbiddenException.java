package com.sims.simscoreservice.exceptions;

/**
 * Thrown when user doesn't have required permissions
 * Maps to HTTP 403 Forbidden
 */
public class ForbiddenException extends RuntimeException{
    public ForbiddenException(String message) {}
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
