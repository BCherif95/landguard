package com.laboussole.domain.port.out.legal;

import com.laboussole.domain.model.legal.LegalDispute;
import com.laboussole.domain.model.parcel.ParcelId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LegalDisputeRepository {
    void save(LegalDispute dispute);
    Optional<LegalDispute> findById(UUID id);
    List<LegalDispute> findByParcelId(ParcelId parcelId);
    List<LegalDispute> findAll();
}
