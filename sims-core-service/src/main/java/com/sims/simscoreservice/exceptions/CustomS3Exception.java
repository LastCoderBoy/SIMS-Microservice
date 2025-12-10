package com.sims.simscoreservice.exceptions;

/**
 * Custom S3 Exception
 * Wraps AWS S3 exceptions with custom messages
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
public class CustomS3Exception extends RuntimeException {

    public CustomS3Exception(String message) {
        super(message);
    }

    public CustomS3Exception(String message, Throwable cause) {
        super(message, cause);
    }
}
