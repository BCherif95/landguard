package com.laboussole.domain.port.in;

import com.laboussole.domain.model.monitoring.SatelliteSnapshot;
import com.laboussole.domain.model.parcel.ParcelId;

public interface GenerateSatelliteSnapshotUseCase {
    SatelliteSnapshot execute(Command command);

    record Command(
            ParcelId parcelId,
            String imageUrl,
            int movementScore,
            int anomalyScore,
            String metadata
    ) {}
}
