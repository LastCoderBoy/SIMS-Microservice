package com.sims.simscoreservice.email.confirmationToken.enums;

import lombok.Getter;

/**
 * Confirmation Token Status Enum
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Getter
public enum ConfirmationTokenStatus {
    PENDING("Awaiting confirmation"),
    CONFIRMED("Confirmed by supplier"),
    CANCELLED("Cancelled by supplier");

    private final String description;

    ConfirmationTokenStatus(String description) {
        this.description = description;
    }
}