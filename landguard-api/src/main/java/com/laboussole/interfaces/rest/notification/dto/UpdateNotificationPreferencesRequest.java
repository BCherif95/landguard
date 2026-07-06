package com.laboussole.interfaces.rest.notification.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateNotificationPreferencesRequest(
        @NotNull(message = "Le choix de la notification push est requis.") Boolean pushEnabled,
        @NotNull(message = "Le choix du canal e-mail est requis.") Boolean emailEnabled,
        @NotNull(message = "Le choix du canal SMS est requis.") Boolean smsEnabled,
        @Pattern(
                regexp = "^\\+[1-9]\\d{7,14}$",
                message = "Le numéro de mobile doit être au format international (ex. +223XXXXXXXX).")
        String phoneNumber) {
}
