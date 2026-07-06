package com.laboussole.infrastructure.notification;

/**
 * Technical delivery failure on one alert channel. Deliberately unchecked and
 * infrastructure-local: the routing orchestrator catches and logs it so a
 * broken channel never blocks the others.
 */
class AlertDeliveryException extends RuntimeException {

    AlertDeliveryException(String message) {
        super(message);
    }

    AlertDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
