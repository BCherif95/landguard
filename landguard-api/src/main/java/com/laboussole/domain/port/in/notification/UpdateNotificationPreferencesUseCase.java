package com.laboussole.domain.port.in.notification;

import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.notification.NotificationPreferences;

public interface UpdateNotificationPreferencesUseCase {

    NotificationPreferences execute(Command command);

    record Command(
            UserId userId,
            boolean pushEnabled,
            boolean emailEnabled,
            boolean smsEnabled,
            String phoneNumber) {}
}
