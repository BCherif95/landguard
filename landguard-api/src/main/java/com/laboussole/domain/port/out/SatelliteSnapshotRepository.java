package com.laboussole.domain.port.out;

import com.laboussole.domain.model.monitoring.SatelliteSnapshot;
import com.laboussole.domain.model.monitoring.SatelliteSnapshotId;
import com.laboussole.domain.model.parcel.ParcelId;

import java.util.List;
import java.util.Optional;

public interface SatelliteSnapshotRepository {
    void save(SatelliteSnapshot snapshot);
    Optional<SatelliteSnapshot> findById(SatelliteSnapshotId id);
    List<SatelliteSnapshot> findByParcelId(ParcelId parcelId);
    List<SatelliteSnapshot> findAll();
}
