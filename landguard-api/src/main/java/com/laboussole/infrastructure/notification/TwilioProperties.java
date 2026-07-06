package com.laboussole.infrastructure.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Twilio SMS credentials — always injected from the environment, never
 * committed. Blank values are tolerated at boot so local dev works without
 * an account; sending then fails loudly and is logged by the router.
 */
@ConfigurationProperties(prefix = "laboussole.notification.sms.twilio")
public record TwilioProperties(
        String accountSid,
        String authToken,
        String fromNumber,
        String apiBaseUrl) {

    public boolean isConfigured() {
        return isNotBlank(accountSid) && isNotBlank(authToken) && isNotBlank(fromNumber);
    }

    private static boolean isNotBlank(String v) {
        return v != null && !v.isBlank();
    }
}
