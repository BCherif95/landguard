package com.laboussole.application.usecase.notification;

import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.notification.NotificationPreferences;
import com.laboussole.domain.port.in.notification.GetNotificationPreferencesUseCase;
import com.laboussole.domain.port.out.NotificationPreferencesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetNotificationPreferencesService implements GetNotificationPreferencesUseCase {

    private final NotificationPreferencesRepository repository;

    public GetNotificationPreferencesService(NotificationPreferencesRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferences execute(UserId userId) {
        return repository.findByUserId(userId)
                .orElseGet(() -> NotificationPreferences.defaults(userId));
    }
}
