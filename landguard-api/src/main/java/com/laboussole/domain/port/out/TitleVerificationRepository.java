package com.laboussole.domain.port.out;

import com.laboussole.domain.model.certification.CertificationId;
import com.laboussole.domain.model.certification.TitleVerificationCase;
import com.laboussole.domain.model.parcel.ParcelId;

import java.util.Optional;

public interface TitleVerificationRepository {
    void save(TitleVerificationCase verificationCase);
    Optional<TitleVerificationCase> findById(CertificationId id);
    Optional<TitleVerificationCase> findByParcelId(ParcelId parcelId);
    Optional<TitleVerificationCase> findByReference(String caseReference);
}
