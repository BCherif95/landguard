package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.heritage.SuccessionAuditEvent;
import com.laboussole.domain.model.heritage.SuccessionPlanId;
import com.laboussole.domain.port.out.heritage.SuccessionAuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SuccessionAuditEventRepositoryAdapter implements SuccessionAuditEventRepository {

    private final SuccessionAuditEventSpringDataRepository repository;

    @Override
    public void save(SuccessionAuditEvent event) {
        SuccessionAuditEventJpaEntity entity = new SuccessionAuditEventJpaEntity(
                event.id(),
                event.successionPlanId().value(),
                event.type(),
                event.actor(),
                event.createdAt(),
                event.metadata()
        );
        repository.save(entity);
    }

    @Override
    public List<SuccessionAuditEvent> findBySuccessionPlanId(SuccessionPlanId planId) {
        return repository.findBySuccessionPlanId(planId.value()).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private SuccessionAuditEvent mapToDomain(SuccessionAuditEventJpaEntity entity) {
        return new SuccessionAuditEvent(
                entity.getId(),
                SuccessionPlanId.of(entity.getSuccessionPlanId()),
                entity.getType(),
                entity.getActor(),
                entity.getCreatedAt(),
                entity.getMetadata()
        );
    }
}
