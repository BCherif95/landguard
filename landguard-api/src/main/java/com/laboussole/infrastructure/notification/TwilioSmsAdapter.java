package com.laboussole.infrastructure.notification;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * {@link SmsGateway} implementation over the Twilio Messages REST API
 * (form-encoded POST with HTTP basic auth — no SDK dependency needed).
 */
@Component
class TwilioSmsAdapter implements SmsGateway {

    private final TwilioProperties properties;
    private final RestClient restClient;

    TwilioSmsAdapter(TwilioProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(properties.apiBaseUrl())
                .build();
    }

    @Override
    public void send(String toPhoneNumber, String body) {
        if (!properties.isConfigured()) {
            throw new AlertDeliveryException(
                    "Twilio SMS gateway is not configured (missing account SID, auth token or from number)");
        }
        var form = new LinkedMultiValueMap<String, String>();
        form.add("To", toPhoneNumber);
        form.add("From", properties.fromNumber());
        form.add("Body", body);

        try {
            restClient.post()
                    .uri("/2010-04-01/Accounts/{sid}/Messages.json", properties.accountSid())
                    .headers(headers -> headers.setBasicAuth(properties.accountSid(), properties.authToken()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
        } catch (AlertDeliveryException e) {
            throw e;
        } catch (Exception e) {
            throw new AlertDeliveryException("Twilio SMS delivery to " + toPhoneNumber + " failed", e);
        }
    }
}
