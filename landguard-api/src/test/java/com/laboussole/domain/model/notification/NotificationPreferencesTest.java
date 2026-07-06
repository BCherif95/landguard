package com.laboussole.domain.model.notification;

import com.laboussole.domain.exception.InvalidNotificationPreferencesException;
import com.laboussole.domain.model.UserId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationPreferencesTest {

    private final UserId userId = UserId.generate();

    @Test
    void defaultsEnablePushAndEmailButNotSms() {
        var preferences = NotificationPreferences.defaults(userId);

        assertThat(preferences.pushEnabled()).isTrue();
        assertThat(preferences.emailEnabled()).isTrue();
        assertThat(preferences.smsEnabled()).isFalse();
        assertThat(preferences.phoneNumber()).isEmpty();
    }

    @Test
    void enablingSmsRequiresPhoneNumber() {
        var preferences = NotificationPreferences.defaults(userId);

        assertThatThrownBy(() -> preferences.update(true, true, true, null))
                .isInstanceOf(InvalidNotificationPreferencesException.class)
                .hasMessage("Un numéro de mobile est requis pour activer les SMS d'urgence.");
    }

    @Test
    void rejectsNonInternationalPhoneNumber() {
        var preferences = NotificationPreferences.defaults(userId);

        assertThatThrownBy(() -> preferences.update(true, true, true, "70000000"))
                .isInstanceOf(InvalidNotificationPreferencesException.class)
                .hasMessage("Le numéro de mobile doit être au format international (ex. +223XXXXXXXX).");
    }

    @Test
    void acceptsMalianMobileNumberAndNormalisesSeparators() {
        var preferences = NotificationPreferences.defaults(userId);

        preferences.update(true, true, true, "+223 70 00 00 00");

        assertThat(preferences.smsEnabled()).isTrue();
        assertThat(preferences.phoneNumber()).contains("+22370000000");
    }

    @Test
    void channelToggleLookupMatchesFlags() {
        var preferences = NotificationPreferences.reconstitute(
                userId, true, false, true, "+22370000000", java.time.Instant.now());

        assertThat(preferences.isChannelEnabled(AlertChannel.PUSH)).isTrue();
        assertThat(preferences.isChannelEnabled(AlertChannel.EMAIL)).isFalse();
        assertThat(preferences.isChannelEnabled(AlertChannel.SMS)).isTrue();
    }
}
