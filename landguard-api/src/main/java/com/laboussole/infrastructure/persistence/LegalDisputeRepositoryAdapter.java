package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.legal.LegalDispute;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.out.legal.LegalDisputeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LegalDisputeRepositoryAdapter implements LegalDisputeRepository {

    private final LegalDisputeSpringDataRepository repository;

    @Override
    public void save(LegalDispute dispute) {
        LegalDisputeJpaEntity entity = new LegalDisputeJpaEntity(
                dispute.id(),
                dispute.parcelId().value(),
                dispute.reason(),
                dispute.severity(),
                dispute.status(),
                dispute.openedAt(),
                dispute.resolvedAt()
        );
        repository.save(entity);
    }

    @Override
    public Optional<LegalDispute> findById(UUID id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<LegalDispute> findByParcelId(ParcelId parcelId) {
        return repository.findByParcelId(parcelId.value()).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<LegalDispute> findAll() {
        return repository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private LegalDispute mapToDomain(LegalDisputeJpaEntity entity) {
        return new LegalDispute(
                entity.getId(),
                ParcelId.of(entity.getParcelId()),
                entity.getReason(),
                entity.getSeverity(),
                entity.getOpenedAt(),
                entity.getResolvedAt(),
                entity.getStatus()
        );
    }
}
