package com.laboussole.domain.port.in;

import com.laboussole.domain.model.monitoring.SatelliteSnapshot;
import com.laboussole.domain.model.parcel.ParcelId;

/**
 * Ingests a new satellite (or drone) image for a parcel. The movement and
 * anomaly scores are computed server-side by comparing the image against the
 * parcel's previous snapshot — callers never supply scores.
 */
public interface IngestSatelliteSnapshotUseCase {

    SatelliteSnapshot execute(Command command);

    record Command(
            ParcelId parcelId,
            byte[] imageContent,
            String fileName,
            String contentType
    ) {}
}
