package com.laboussole.infrastructure.notification;

/**
 * Provider-agnostic SMS sending seam. {@link SmsAlertAdapter} depends on this
 * interface only, so swapping Twilio for Orange Mali / Malitel later means
 * writing one new implementation, nothing else.
 */
public interface SmsGateway {

    /**
     * Sends a single text message.
     *
     * @param toPhoneNumber recipient in E.164 format (e.g. {@code +223XXXXXXXX})
     * @param body          message text, at most 160 characters
     * @throws AlertDeliveryException on provider failure
     */
    void send(String toPhoneNumber, String body);
}
