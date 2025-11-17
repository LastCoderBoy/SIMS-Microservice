package com.sims.common.exceptions.handler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Standard error response structure
 *
 * @author LastCoderBoy
 * @since 2025-01-17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int status;
    private String error;
    private String message;
    private Date timestamp;
}
