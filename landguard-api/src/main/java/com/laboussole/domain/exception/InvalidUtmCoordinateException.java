package com.laboussole.domain.exception;

/**
 * Raised when a UTM coordinate is outside the ranges accepted for Malian
 * territory (zones 29N/30N). The message is user-facing French — it is
 * surfaced as-is by the REST layer's generic {@code DomainException} handler.
 */
public class InvalidUtmCoordinateException extends DomainException {

    public static final String CODE = "INVALID_UTM_COORDINATE";

    public InvalidUtmCoordinateException(String message) {
        super(CODE, message);
    }
}
