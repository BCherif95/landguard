package com.laboussole.domain.port.in.legal;

import com.laboussole.domain.model.legal.DisputeSeverity;
import com.laboussole.domain.model.parcel.ParcelId;

import java.util.UUID;

public interface OpenLegalDisputeUseCase {
    UUID open(ParcelId parcelId, String reason, DisputeSeverity severity);
}
