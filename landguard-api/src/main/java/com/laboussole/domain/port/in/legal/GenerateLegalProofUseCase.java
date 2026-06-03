package com.laboussole.domain.port.in.legal;

import com.laboussole.application.usecase.legal.LegalDossier;
import com.laboussole.domain.model.parcel.ParcelId;

public interface GenerateLegalProofUseCase {
    LegalDossier generate(ParcelId parcelId);
}
