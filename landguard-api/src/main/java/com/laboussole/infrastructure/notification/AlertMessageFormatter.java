package com.laboussole.infrastructure.notification;

import com.laboussole.domain.model.monitoring.MonitoringSeverity;

/** Shared French wording for alert messages across channels. */
final class AlertMessageFormatter {

    private AlertMessageFormatter() {
    }

    static String severityLabel(MonitoringSeverity severity) {
        return switch (severity) {
            case LOW -> "Faible";
            case MEDIUM -> "Moyenne";
            case HIGH -> "Élevée";
            case CRITICAL -> "Critique";
        };
    }
}
