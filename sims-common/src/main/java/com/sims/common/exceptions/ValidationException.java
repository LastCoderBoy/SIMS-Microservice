package com.sims.common.exceptions;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when the validation of a request fails
 *
 * @author LastCoderBoy
 * @since 2025-01-17
 */
@Data
public class ValidationException extends RuntimeException {
    private final List<String> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = Collections.singletonList(message);
    }

    public ValidationException(List<String> errors) {
        super(String.join(", ", errors));
        this.errors = errors;
    }
}
