package com.laboussole.domain.port.in.notification;

import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.notification.NotificationPreferences;

public interface GetNotificationPreferencesUseCase {

    /** Returns stored preferences, or the defaults if the user never saved any. */
    NotificationPreferences execute(UserId userId);
}
