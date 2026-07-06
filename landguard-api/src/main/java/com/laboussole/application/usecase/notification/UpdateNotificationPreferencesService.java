package com.laboussole.application.usecase.notification;

import com.laboussole.domain.model.notification.NotificationPreferences;
import com.laboussole.domain.port.in.notification.UpdateNotificationPreferencesUseCase;
import com.laboussole.domain.port.out.NotificationPreferencesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateNotificationPreferencesService implements UpdateNotificationPreferencesUseCase {

    private final NotificationPreferencesRepository repository;

    public UpdateNotificationPreferencesService(NotificationPreferencesRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public NotificationPreferences execute(Command command) {
        var preferences = repository.findByUserId(command.userId())
                .orElseGet(() -> NotificationPreferences.defaults(command.userId()));
        preferences.update(
                command.pushEnabled(),
                command.emailEnabled(),
                command.smsEnabled(),
                command.phoneNumber());
        return repository.save(preferences);
    }
}
