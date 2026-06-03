package com.laboussole.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.locationtech.jts.geom.Polygon;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface LandParcelSpringDataRepository extends JpaRepository<LandParcelJpaEntity, UUID> {

    Optional<LandParcelJpaEntity> findByReference(String reference);

    boolean existsByReference(String reference);

    List<LandParcelJpaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<LandParcelJpaEntity> findByOwnerUserIdOrderByCreatedAtDesc(UUID ownerUserId, Pageable pageable);

    @Query("SELECT p FROM LandParcelJpaEntity p WHERE intersects(p.geometry, :geometry) = true")
    List<LandParcelJpaEntity> findOverlappingParcels(@Param("geometry") Polygon geometry);
}
