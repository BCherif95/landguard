package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.monitoring.MonitoringEventId;
import com.laboussole.domain.model.notification.AlertDelivery;
import com.laboussole.domain.model.notification.AlertDeliveryId;
import com.laboussole.domain.model.notification.AlertDeliveryStatus;
import com.laboussole.domain.port.out.AlertDeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
class AlertDeliveryRepositoryAdapter implements AlertDeliveryRepository {

    private final AlertDeliverySpringDataRepository repository;

    @Override
    public void save(AlertDelivery delivery) {
        repository.save(toEntity(delivery));
    }

    @Override
    public void saveAll(List<AlertDelivery> deliveries) {
        repository.saveAll(deliveries.stream().map(AlertDeliveryRepositoryAdapter::toEntity).toList());
    }

    /**
     * Claim and flag in a single short transaction: the rows come back locked
     * with SKIP LOCKED, are switched to IN_FLIGHT, and the lock is released on
     * commit — before any gateway is contacted.
     */
    @Override
    @Transactional
    public List<AlertDelivery> claimDueForDispatch(int limit, Instant now) {
        var due = repository.lockDueForDispatch(now, PageRequest.ofSize(limit));
        if (due.isEmpty()) {
            return List.of();
        }
        var claimed = due.stream().map(AlertDeliveryRepositoryAdapter::toDomain).toList();
        claimed.forEach(delivery -> delivery.markInFlight(now));
        repository.saveAll(claimed.stream().map(AlertDeliveryRepositoryAdapter::toEntity).toList());
        return claimed;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertDelivery> findStaleInFlight(Instant staleBefore, int limit) {
        return repository
                .findByStatusAndUpdatedAtBeforeOrderByUpdatedAtAsc(
                        AlertDeliveryStatus.IN_FLIGHT, staleBefore, PageRequest.ofSize(limit))
                .stream()
                .map(AlertDeliveryRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertDelivery> findByEventId(MonitoringEventId eventId) {
        return repository.findByEventIdOrderByDispatchPriorityAscCreatedAtDesc(eventId.value())
                .stream()
                .map(AlertDeliveryRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(AlertDeliveryStatus status) {
        return repository.countByStatus(status);
    }

    private static AlertDeliveryJpaEntity toEntity(AlertDelivery delivery) {
        return new AlertDeliveryJpaEntity(
                delivery.id().value(),
                delivery.eventId().value(),
                delivery.recipientId().value(),
                delivery.channel(),
                delivery.channel().dispatchPriority(),
                delivery.status(),
                delivery.attemptCount(),
                delivery.nextAttemptAt(),
                delivery.lastError().orElse(null),
                delivery.createdAt(),
                delivery.updatedAt(),
                delivery.deliveredAt().orElse(null));
    }

    private static AlertDelivery toDomain(AlertDeliveryJpaEntity entity) {
        return AlertDelivery.reconstitute(
                new AlertDeliveryId(entity.getId()),
                new MonitoringEventId(entity.getEventId()),
                new UserId(entity.getRecipientUserId()),
                entity.getChannel(),
                entity.getStatus(),
                entity.getAttemptCount(),
                entity.getNextAttemptAt(),
                entity.getLastError(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeliveredAt());
    }
}
