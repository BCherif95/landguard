package com.laboussole.domain.model.certification;

import com.laboussole.domain.model.parcel.Hectares;

import java.time.Instant;

public record LandTitle(
        String tfNumber,
        String volume,
        String folio,
        String conservationOffice,
        Instant issueDate,
        String ownerName,
        Hectares area,
        String location,
        String titleStatus
) {}
