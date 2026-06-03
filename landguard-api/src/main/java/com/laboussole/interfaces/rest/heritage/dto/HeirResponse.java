package com.laboussole.interfaces.rest.heritage.dto;

import com.laboussole.domain.model.heritage.Heir;

import java.util.UUID;

public record HeirResponse(
        UUID id,
        String fullName,
        String relation,
        int sharePercentage,
        boolean validated
) {
    public static HeirResponse from(Heir heir) {
        return new HeirResponse(
                heir.id().value(),
                heir.fullName(),
                heir.relation(),
                heir.sharePercentage(),
                heir.validated()
        );
    }
}
