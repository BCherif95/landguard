package com.laboussole.domain.model.notification;

import com.laboussole.domain.exception.InvalidNotificationPreferencesException;
import com.laboussole.domain.model.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Per-user routing preferences for monitoring alerts.
 *
 * <p>Invariants:
 * <ul>
 *   <li>SMS can only be enabled when a phone number is present;</li>
 *   <li>the phone number, when present, must be in international E.164
 *       format (Mali mobile numbers are {@code +223XXXXXXXX}).</li>
 * </ul>
 */
public final class NotificationPreferences {

    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    private final UserId userId;
    private boolean pushEnabled;
    private boolean emailEnabled;
    private boolean smsEnabled;
    private String phoneNumber;
    private Instant updatedAt;

    private NotificationPreferences(
            UserId userId,
            boolean pushEnabled,
            boolean emailEnabled,
            boolean smsEnabled,
            String phoneNumber,
            Instant updatedAt) {
        this.userId = Objects.requireNonNull(userId);
        this.pushEnabled = pushEnabled;
        this.emailEnabled = emailEnabled;
        this.smsEnabled = smsEnabled;
        this.phoneNumber = normalisePhone(phoneNumber);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        enforceInvariants();
    }

    /** Push and e-mail on, SMS off until the user provides a phone number. */
    public static NotificationPreferences defaults(UserId userId) {
        return new NotificationPreferences(userId, true, true, false, null, Instant.now());
    }

    public static NotificationPreferences reconstitute(
            UserId userId,
            boolean pushEnabled,
            boolean emailEnabled,
            boolean smsEnabled,
            String phoneNumber,
            Instant updatedAt) {
        return new NotificationPreferences(userId, pushEnabled, emailEnabled, smsEnabled, phoneNumber, updatedAt);
    }

    public void update(boolean pushEnabled, boolean emailEnabled, boolean smsEnabled, String phoneNumber) {
        this.pushEnabled = pushEnabled;
        this.emailEnabled = emailEnabled;
        this.smsEnabled = smsEnabled;
        this.phoneNumber = normalisePhone(phoneNumber);
        this.updatedAt = Instant.now();
        enforceInvariants();
    }

    public boolean isChannelEnabled(AlertChannel channel) {
        return switch (channel) {
            case PUSH -> pushEnabled;
            case EMAIL -> emailEnabled;
            case SMS -> smsEnabled;
        };
    }

    private void enforceInvariants() {
        if (smsEnabled && phoneNumber == null) {
            throw new InvalidNotificationPreferencesException(
                    "Un numéro de mobile est requis pour activer les SMS d'urgence.");
        }
        if (phoneNumber != null && !E164_PATTERN.matcher(phoneNumber).matches()) {
            throw new InvalidNotificationPreferencesException(
                    "Le numéro de mobile doit être au format international (ex. +223XXXXXXXX).");
        }
    }

    private static String normalisePhone(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.replaceAll("[\\s.-]", "");
    }

    public UserId userId() { return userId; }
    public boolean pushEnabled() { return pushEnabled; }
    public boolean emailEnabled() { return emailEnabled; }
    public boolean smsEnabled() { return smsEnabled; }
    public Optional<String> phoneNumber() { return Optional.ofNullable(phoneNumber); }
    public Instant updatedAt() { return updatedAt; }
}
