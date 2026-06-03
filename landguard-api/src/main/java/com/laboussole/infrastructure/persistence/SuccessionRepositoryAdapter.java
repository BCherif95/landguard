package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.heritage.Heir;
import com.laboussole.domain.model.heritage.HeirId;
import com.laboussole.domain.model.heritage.SuccessionPlan;
import com.laboussole.domain.model.heritage.SuccessionPlanId;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.out.SuccessionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SuccessionRepositoryAdapter implements SuccessionRepository {

    private final SuccessionPlanSpringDataRepository springDataRepository;

    public SuccessionRepositoryAdapter(SuccessionPlanSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public SuccessionPlan save(SuccessionPlan plan) {
        var entity = toEntity(plan);
        var saved = springDataRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<SuccessionPlan> findById(SuccessionPlanId id) {
        return springDataRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<SuccessionPlan> findByParcelId(ParcelId parcelId) {
        return springDataRepository.findByParcelId(parcelId.value()).map(this::toDomain);
    }

    @Override
    public List<SuccessionPlan> findAll() {
        return springDataRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    private SuccessionPlanJpaEntity toEntity(SuccessionPlan plan) {
        var entity = new SuccessionPlanJpaEntity();
        entity.setId(plan.id().value());
        entity.setParcelId(plan.parcelId().value());
        entity.setStatus(plan.status());
        entity.setBlockchainHash(plan.blockchainHash());
        entity.setCreatedAt(plan.createdAt());
        entity.setUpdatedAt(plan.updatedAt());
        entity.setHeirs(plan.heirs().stream()
                .map(h -> {
                    var hEntity = new HeirJpaEntity();
                    hEntity.setId(h.id().value());
                    hEntity.setPlan(entity);
                    hEntity.setFullName(h.fullName());
                    hEntity.setRelation(h.relation());
                    hEntity.setSharePercentage(h.sharePercentage());
                    hEntity.setValidated(h.validated());
                    return hEntity;
                }).collect(Collectors.toList()));
        return entity;
    }

    private SuccessionPlan toDomain(SuccessionPlanJpaEntity entity) {
        var heirs = entity.getHeirs().stream()
                .map(h -> new Heir(
                        new HeirId(h.getId()),
                        h.getFullName(),
                        h.getRelation(),
                        h.getSharePercentage(),
                        h.isValidated()
                )).collect(Collectors.toList());

        return new SuccessionPlan(
                new SuccessionPlanId(entity.getId()),
                new ParcelId(entity.getParcelId()),
                heirs,
                entity.getStatus(),
                entity.getBlockchainHash(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
