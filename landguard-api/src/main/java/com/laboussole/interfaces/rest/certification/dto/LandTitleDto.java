package com.laboussole.interfaces.rest.certification.dto;

import com.laboussole.domain.model.certification.LandTitle;
import com.laboussole.domain.model.parcel.Hectares;

import java.time.Instant;

public record LandTitleDto(
        String tfNumber,
        String volume,
        String folio,
        String conservationOffice,
        Instant issueDate,
        String ownerName,
        double areaHectares,
        String location,
        String status
) {
    public static LandTitleDto fromDomain(LandTitle domain) {
        return new LandTitleDto(
                domain.tfNumber(),
                domain.volume(),
                domain.folio(),
                domain.conservationOffice(),
                domain.issueDate(),
                domain.ownerName(),
                domain.area().value().doubleValue(),
                domain.location(),
                domain.titleStatus()
        );
    }

    public LandTitle toDomain() {
        return new LandTitle(
                tfNumber,
                volume,
                folio,
                conservationOffice,
                issueDate,
                ownerName,
                new Hectares(java.math.BigDecimal.valueOf(areaHectares)),
                location,
                status
        );
    }
}
