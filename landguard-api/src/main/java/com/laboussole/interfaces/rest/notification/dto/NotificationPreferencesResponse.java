package com.laboussole.interfaces.rest.notification.dto;

import com.laboussole.domain.model.notification.NotificationPreferences;

public record NotificationPreferencesResponse(
        boolean pushEnabled,
        boolean emailEnabled,
        boolean smsEnabled,
        String phoneNumber) {

    public static NotificationPreferencesResponse from(NotificationPreferences preferences) {
        return new NotificationPreferencesResponse(
                preferences.pushEnabled(),
                preferences.emailEnabled(),
                preferences.smsEnabled(),
                preferences.phoneNumber().orElse(null));
    }
}
