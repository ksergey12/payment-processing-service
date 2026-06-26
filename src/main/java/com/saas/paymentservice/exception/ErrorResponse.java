package com.saas.paymentservice.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> validationErrors
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(Instant.now(), status, error, message, null);
    }

    public static ErrorResponse ofValidation(int status, String error, String message,
                                             Map<String, String> validationErrors) {
        return new ErrorResponse(Instant.now(), status, error, message, validationErrors);
    }
}
