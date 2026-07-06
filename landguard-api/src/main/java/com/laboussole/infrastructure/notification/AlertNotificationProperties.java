package com.laboussole.infrastructure.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Alert-notification settings shared by the delivery adapters.
 *
 * @param emailFrom       sender address for alert e-mails
 * @param frontendBaseUrl public URL of the web app, used to build deep links
 *                        (evidence dossier) embedded in messages
 */
@ConfigurationProperties(prefix = "laboussole.notification")
public record AlertNotificationProperties(String emailFrom, String frontendBaseUrl) {
}
