package com.sims.authservice.dto;

public record TokenValidationResponse(boolean valid, String message) {}
