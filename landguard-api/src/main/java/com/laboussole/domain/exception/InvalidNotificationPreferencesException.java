package com.laboussole.domain.exception;

/**
 * Raised when alert-notification preferences violate a business rule
 * (e.g. enabling SMS without a phone number). Messages are user-facing
 * French — surfaced verbatim by the REST layer.
 */
public class InvalidNotificationPreferencesException extends DomainException {

    public static final String CODE = "INVALID_NOTIFICATION_PREFERENCES";

    public InvalidNotificationPreferencesException(String message) {
        super(CODE, message);
    }
}
