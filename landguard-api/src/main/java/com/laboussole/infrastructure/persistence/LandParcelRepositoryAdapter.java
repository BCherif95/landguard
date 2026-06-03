package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.parcel.CadastralReference;
import com.laboussole.domain.model.parcel.Hectares;
import com.laboussole.domain.model.parcel.LandDocument;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.MoneyXof;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.out.LandParcelRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
class LandParcelRepositoryAdapter implements LandParcelRepository {

    private final LandParcelSpringDataRepository jpa;

    LandParcelRepositoryAdapter(LandParcelSpringDataRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public LandParcel save(LandParcel parcel) {
        var existing = jpa.findById(parcel.id().value()).orElse(null);
        var documentEntities = parcel.documents().stream()
                .map(d -> new LandDocumentJpaEntity(
                        d.id(),
                        parcel.id().value(),
                        d.type(),
                        d.label(),
                        d.storageKey(),
                        d.fileName(),
                        d.contentType(),
                        d.uploadedAt()))
                .toList();

        if (existing == null) {
            var entity = new LandParcelJpaEntity(
                    parcel.id().value(),
                    parcel.reference().value(),
                    parcel.name(),
                    parcel.regionLabel(),
                    parcel.ownerLabel(),
                    parcel.ownerUserId() == null ? null : parcel.ownerUserId().value(),
                    parcel.area().value(),
                    parcel.estimatedValue().amount(),
                    JtsGeometryFactory.toPolygon(parcel.geometry()),
                    parcel.status(),
                    parcel.riskScore(),
                    parcel.trustScore(),
                    parcel.titleNumber(),
                    parcel.createdAt(),
                    parcel.updatedAt(),
                    parcel.lastVerifiedAt(),
                    documentEntities);
            return toDomain(jpa.save(entity));
        }
        existing.setName(parcel.name());
        existing.setRegionLabel(parcel.regionLabel());
        existing.setOwnerLabel(parcel.ownerLabel());
        existing.setAreaHa(parcel.area().value());
        existing.setEstimatedValueXof(parcel.estimatedValue().amount());
        existing.setGeometry(JtsGeometryFactory.toPolygon(parcel.geometry()));
        existing.setStatus(parcel.status());
        existing.setRiskScore(parcel.riskScore());
        existing.setTrustScore(parcel.trustScore());
        existing.setTitleNumber(parcel.titleNumber());
        existing.setUpdatedAt(parcel.updatedAt());
        existing.setLastVerifiedAt(parcel.lastVerifiedAt());
        
        // Update documents
        existing.getDocuments().clear();
        existing.getDocuments().addAll(documentEntities);
        
        return toDomain(jpa.save(existing));
    }

    @Override
    public Optional<LandParcel> findById(ParcelId id) {
        return jpa.findById(id.value()).map(LandParcelRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<LandParcel> findByReference(CadastralReference reference) {
        return jpa.findByReference(reference.value()).map(LandParcelRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existsByReference(CadastralReference reference) {
        return jpa.existsByReference(reference.value());
    }

    @Override
    public List<LandParcel> findAll(int limit) {
        return jpa.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit)).stream()
                .map(LandParcelRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public List<LandParcel> findByOwner(UserId ownerUserId, int limit) {
        return jpa.findByOwnerUserIdOrderByCreatedAtDesc(ownerUserId.value(), PageRequest.of(0, limit)).stream()
                .map(LandParcelRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public List<LandParcel> findOverlappingParcels(com.laboussole.domain.model.parcel.ParcelGeometry geometry) {
        return jpa.findOverlappingParcels(JtsGeometryFactory.toPolygon(geometry)).stream()
                .map(LandParcelRepositoryAdapter::toDomain)
                .toList();
    }

    private static LandParcel toDomain(LandParcelJpaEntity e) {
        var docs = e.getDocuments().stream()
                .map(d -> new LandDocument(
                        d.getId(),
                        d.getType(),
                        d.getLabel(),
                        d.getStorageKey(),
                        d.getFileName(),
                        d.getContentType(),
                        d.getUploadedAt()))
                .toList();

        return LandParcel.reconstitute(
                ParcelId.of(e.getId()),
                CadastralReference.of(e.getReference()),
                e.getName(),
                e.getRegionLabel(),
                e.getOwnerLabel(),
                e.getOwnerUserId() == null ? null : UserId.of(e.getOwnerUserId()),
                Hectares.of(e.getAreaHa()),
                new MoneyXof(e.getEstimatedValueXof()),
                JtsGeometryFactory.toDomain(e.getGeometry()),
                e.getStatus(),
                e.getRiskScore(),
                e.getTrustScore(),
                e.getTitleNumber(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getLastVerifiedAt(),
                docs);
    }
}
