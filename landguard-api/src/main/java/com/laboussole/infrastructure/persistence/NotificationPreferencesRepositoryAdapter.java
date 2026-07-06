package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.notification.NotificationPreferences;
import com.laboussole.domain.port.out.NotificationPreferencesRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class NotificationPreferencesRepositoryAdapter implements NotificationPreferencesRepository {

    private final NotificationPreferencesSpringDataRepository jpa;

    NotificationPreferencesRepositoryAdapter(NotificationPreferencesSpringDataRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public NotificationPreferences save(NotificationPreferences preferences) {
        var existing = jpa.findById(preferences.userId().value()).orElse(null);
        if (existing == null) {
            var entity = new NotificationPreferencesJpaEntity(
                    preferences.userId().value(),
                    preferences.pushEnabled(),
                    preferences.emailEnabled(),
                    preferences.smsEnabled(),
                    preferences.phoneNumber().orElse(null),
                    preferences.updatedAt());
            return toDomain(jpa.save(entity));
        }
        existing.setPushEnabled(preferences.pushEnabled());
        existing.setEmailEnabled(preferences.emailEnabled());
        existing.setSmsEnabled(preferences.smsEnabled());
        existing.setPhoneNumber(preferences.phoneNumber().orElse(null));
        existing.setUpdatedAt(preferences.updatedAt());
        return toDomain(jpa.save(existing));
    }

    @Override
    public Optional<NotificationPreferences> findByUserId(UserId userId) {
        return jpa.findById(userId.value()).map(NotificationPreferencesRepositoryAdapter::toDomain);
    }

    private static NotificationPreferences toDomain(NotificationPreferencesJpaEntity e) {
        return NotificationPreferences.reconstitute(
                UserId.of(e.getUserId()),
                e.isPushEnabled(),
                e.isEmailEnabled(),
                e.isSmsEnabled(),
                e.getPhoneNumber(),
                e.getUpdatedAt());
    }
}
