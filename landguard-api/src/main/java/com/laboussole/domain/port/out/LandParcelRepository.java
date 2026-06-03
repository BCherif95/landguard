package com.laboussole.domain.port.out;

import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.parcel.CadastralReference;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.ParcelId;

import java.util.List;
import java.util.Optional;

/** Driven port: persistence for the {@link LandParcel} aggregate. */
public interface LandParcelRepository {

    LandParcel save(LandParcel parcel);

    Optional<LandParcel> findById(ParcelId id);

    Optional<LandParcel> findByReference(CadastralReference reference);

    boolean existsByReference(CadastralReference reference);

    /** Most-recent-first listing — pagination layered on top later. */
    List<LandParcel> findAll(int limit);

    List<LandParcel> findByOwner(UserId ownerUserId, int limit);

    List<LandParcel> findOverlappingParcels(com.laboussole.domain.model.parcel.ParcelGeometry geometry);
}
