package com.sims.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Standard API response wrapper for all SIMS microservices
 * Provides a consistent response structure across all services
 *
 * @param <T> Type of data payload
 * @author LastCoderBoy
 * @since 2025-01-17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private T data;

    /**
     * Constructor for responses without data payload
     *
     * @param success operation success status
     * @param message response message
     */
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null;
    }

    /**
     * Factory method for successful response with data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Factory method for successful response without data
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    /**
     * Factory method for error response
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    /**
     * Factory method for error response with data (e.g., validation errors)
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}
