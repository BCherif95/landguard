package com.laboussole.domain.port.out;

import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.notification.NotificationPreferences;

import java.util.Optional;

/** Driven port: persistence for {@link NotificationPreferences}. */
public interface NotificationPreferencesRepository {

    NotificationPreferences save(NotificationPreferences preferences);

    Optional<NotificationPreferences> findByUserId(UserId userId);
}
