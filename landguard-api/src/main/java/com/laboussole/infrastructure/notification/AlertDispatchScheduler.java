package com.laboussole.infrastructure.notification;

import com.laboussole.application.service.AlertDispatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Beats the durable alert queue.
 *
 * <p>{@code fixedDelay} rather than {@code fixedRate}: a slow carrier must not
 * cause drain cycles to pile up on top of each other.
 */
@Component
@Slf4j
class AlertDispatchScheduler {

    private final AlertDispatchService dispatchService;

    AlertDispatchScheduler(AlertDispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @Scheduled(
            fixedDelayString = "${laboussole.notification.dispatch.interval-ms:10000}",
            initialDelayString = "${laboussole.notification.dispatch.initial-delay-ms:15000}")
    void drainQueue() {
        try {
            dispatchService.drain();
        } catch (Exception e) {
            // A broken cycle must never kill the scheduler thread — the next
            // beat has to keep trying, otherwise alerting stops silently.
            log.error("Alert dispatch cycle failed: {}", e.getMessage(), e);
        }
    }
}
