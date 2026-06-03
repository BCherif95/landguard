package com.laboussole.interfaces.rest.error;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        List<FieldViolation> errors) {

    public static ApiError of(int status, String code, String message, String path) {
        return new ApiError(Instant.now(), status, code, message, path, List.of());
    }

    public static ApiError of(int status, String code, String message, String path, List<FieldViolation> errors) {
        return new ApiError(Instant.now(), status, code, message, path, errors);
    }

    public record FieldViolation(String field, String message) {}
}
