package com.laboussole.infrastructure.config;

import com.laboussole.infrastructure.notification.AlertDispatchProperties;
import com.laboussole.infrastructure.notification.AlertNotificationProperties;
import com.laboussole.infrastructure.notification.TwilioProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for alert delivery.
 *
 * <p>Replaces the former {@code AlertAsyncConfig}: alerts are no longer
 * dispatched on an in-memory executor, so the thread pool is gone, but the
 * properties it registered are still needed.
 */
@Configuration
@EnableConfigurationProperties({
        AlertNotificationProperties.class,
        TwilioProperties.class,
        AlertDispatchProperties.class
})
class AlertNotificationConfig {
}
