package com.laboussole.infrastructure.notification;

import com.laboussole.domain.model.User;
import com.laboussole.domain.model.monitoring.MonitoringEvent;
import com.laboussole.domain.model.notification.AlertChannel;
import com.laboussole.domain.port.out.AlertNotificationPort;
import com.laboussole.domain.port.out.LandParcelRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * E-mail channel: detailed HTML alert aimed at diaspora owners, with the
 * "RÉPUBLIQUE DU MALI" header used by the official PDFs
 * ({@code PdfGenerationService}) and a deep link to the evidence dossier.
 */
@Component
class EmailAlertAdapter implements AlertNotificationPort {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
            .ofPattern("d MMMM yyyy 'à' HH:mm '(UTC)'", Locale.FRENCH)
            .withZone(ZoneId.of("UTC"));

    private final JavaMailSender mailSender;
    private final LandParcelRepository parcelRepository;
    private final AlertNotificationProperties properties;

    EmailAlertAdapter(
            JavaMailSender mailSender,
            LandParcelRepository parcelRepository,
            AlertNotificationProperties properties) {
        this.mailSender = mailSender;
        this.parcelRepository = parcelRepository;
        this.properties = properties;
    }

    @Override
    public AlertChannel channel() {
        return AlertChannel.EMAIL;
    }

    @Override
    public void dispatch(MonitoringEvent event, User recipient) {
        var parcelReference = parcelRepository.findById(event.parcelId())
                .map(parcel -> parcel.reference().value())
                .orElse(event.parcelId().value().toString());
        try {
            MimeMessage message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.emailFrom());
            helper.setTo(recipient.email().value());
            helper.setSubject("ALERTE LA BOUSSOLE — Activité suspecte sur votre parcelle " + parcelReference);
            helper.setText(buildHtmlBody(event, recipient, parcelReference), true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new AlertDeliveryException(
                    "Failed to send alert e-mail to " + recipient.email().value(), e);
        }
    }

    private String buildHtmlBody(MonitoringEvent event, User recipient, String parcelReference) {
        var dossierLink = properties.frontendBaseUrl() + "/preuves?parcelId=" + event.parcelId().value();
        return """
                <!DOCTYPE html>
                <html lang="fr">
                <body style="font-family: Helvetica, Arial, sans-serif; color: #1a1a1a; margin: 0; padding: 24px; background: #f4f4f4;">
                  <div style="max-width: 600px; margin: 0 auto; background: #ffffff; border: 1px solid #dddddd; border-radius: 8px; overflow: hidden;">
                    <div style="text-align: center; padding: 24px; border-bottom: 3px solid #10b981;">
                      <p style="font-size: 16px; font-weight: bold; margin: 0;">RÉPUBLIQUE DU MALI</p>
                      <p style="font-size: 12px; margin: 4px 0 0;">Un Peuple - Un But - Une Foi</p>
                      <p style="font-size: 11px; color: #555555; margin: 12px 0 0;">
                        MINISTÈRE DE L'URBANISME, DE L'HABITAT,<br/>
                        DES DOMAINES, DE L'AMÉNAGEMENT DU TERRITOIRE ET DE LA POPULATION<br/>
                        — DIRECTION NATIONALE DES DOMAINES ET DU CADASTRE —
                      </p>
                    </div>
                    <div style="padding: 24px;">
                      <h1 style="font-size: 18px; color: #b91c1c; margin: 0 0 16px;">Alerte de surveillance foncière</h1>
                      <p>Bonjour %s,</p>
                      <p>Une activité suspecte a été détectée sur votre parcelle <strong>%s</strong>.</p>
                      <table style="width: 100%%; border-collapse: collapse; font-size: 14px; margin: 16px 0;">
                        <tr><td style="padding: 6px 0; color: #555555;">Sévérité</td><td style="padding: 6px 0;"><strong>%s</strong></td></tr>
                        <tr><td style="padding: 6px 0; color: #555555;">Détection</td><td style="padding: 6px 0;">%s</td></tr>
                        <tr><td style="padding: 6px 0; color: #555555;">Indice de confiance</td><td style="padding: 6px 0;">%d %%</td></tr>
                        <tr><td style="padding: 6px 0; color: #555555;">Description</td><td style="padding: 6px 0;">%s</td></tr>
                      </table>
                      <p style="text-align: center; margin: 24px 0;">
                        <a href="%s" style="display: inline-block; background: #10b981; color: #ffffff; text-decoration: none; padding: 12px 24px; border-radius: 6px; font-weight: bold;">
                          Consulter le dossier de preuve
                        </a>
                      </p>
                      <p style="font-size: 13px; color: #555555;">
                        Si vous n'êtes pas à l'origine de cette activité, nous vous recommandons de contacter
                        votre notaire ou la Direction Nationale des Domaines et du Cadastre dans les plus brefs délais.
                      </p>
                    </div>
                    <div style="padding: 16px 24px; background: #fafafa; border-top: 1px solid #eeeeee; font-size: 11px; color: #888888; text-align: center;">
                      LA BOUSSOLE — Plateforme nationale de sécurisation foncière.<br/>
                      Ce message est envoyé automatiquement, merci de ne pas y répondre.
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                HtmlUtils.htmlEscape(recipient.fullName()),
                HtmlUtils.htmlEscape(parcelReference),
                AlertMessageFormatter.severityLabel(event.severity()),
                DATE_FORMAT.format(event.detectedAt()),
                event.confidenceScore(),
                HtmlUtils.htmlEscape(event.description()),
                dossierLink);
    }
}
