package com.laboussole.infrastructure.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Tuning for the durable alert dispatcher.
 *
 * @param batchSize     deliveries claimed per drain cycle
 * @param maxAttempts   attempts allowed before a delivery is dead-lettered
 * @param baseBackoff   wait after the first failure; doubles on each retry
 * @param staleAfter    how long an IN_FLIGHT delivery may sit before it is
 *                      considered abandoned by a dead worker and requeued
 */
@ConfigurationProperties(prefix = "laboussole.notification.dispatch")
public record AlertDispatchProperties(
        int batchSize,
        int maxAttempts,
        Duration baseBackoff,
        Duration staleAfter
) {
    public AlertDispatchProperties {
        if (batchSize <= 0) {
            batchSize = 50;
        }
        if (maxAttempts <= 0) {
            maxAttempts = 5;
        }
        if (baseBackoff == null || baseBackoff.isNegative() || baseBackoff.isZero()) {
            baseBackoff = Duration.ofMinutes(1);
        }
        if (staleAfter == null || staleAfter.isNegative() || staleAfter.isZero()) {
            staleAfter = Duration.ofMinutes(10);
        }
    }
}
