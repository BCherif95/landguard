package com.laboussole.infrastructure.notification;

import com.laboussole.domain.model.User;
import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.model.notification.AlertChannel;
import com.laboussole.domain.port.out.AlertNotificationPort;
import com.laboussole.domain.port.out.LandParcelRepository;
import com.laboussole.domain.port.out.NotificationPreferencesRepository;
import org.springframework.stereotype.Component;

/**
 * Urgency SMS channel — the fallback for owners with poor Internet coverage.
 * Message is fixed French wording, hard-capped at 160 characters (single
 * GSM segment).
 */
@Component
class SmsAlertAdapter implements AlertNotificationPort {

    private static final int MAX_SMS_LENGTH = 160;
    private static final String MESSAGE_TEMPLATE =
            "ALERTE LA BOUSSOLE : Activité suspecte détectée sur votre parcelle %s."
                    + " Consultez l'application ou appelez votre notaire.";

    private final SmsGateway smsGateway;
    private final LandParcelRepository parcelRepository;
    private final NotificationPreferencesRepository preferencesRepository;

    SmsAlertAdapter(
            SmsGateway smsGateway,
            LandParcelRepository parcelRepository,
            NotificationPreferencesRepository preferencesRepository) {
        this.smsGateway = smsGateway;
        this.parcelRepository = parcelRepository;
        this.preferencesRepository = preferencesRepository;
    }

    @Override
    public AlertChannel channel() {
        return AlertChannel.SMS;
    }

    @Override
    public void dispatch(MonitoringEvent event, User recipient) {
        var phoneNumber = preferencesRepository.findByUserId(recipient.id())
                .flatMap(preferences -> preferences.phoneNumber())
                .orElseThrow(() -> new AlertDeliveryException(
                        "No phone number on record for user " + recipient.id().value()
                                + " — SMS channel enabled without a number"));

        var parcelReference = parcelRepository.findById(event.parcelId())
                .map(parcel -> parcel.reference().value())
                .orElse(event.parcelId().value().toString());

        var body = MESSAGE_TEMPLATE.formatted(parcelReference);
        if (body.length() > MAX_SMS_LENGTH) {
            body = body.substring(0, MAX_SMS_LENGTH);
        }
        smsGateway.send(phoneNumber, body);
    }
}
