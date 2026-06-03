package com.laboussole.domain.exception;

import com.laboussole.domain.model.parcel.ParcelId;
import java.util.List;

public class CadastralOverlapException extends DomainException {
    public CadastralOverlapException(List<ParcelId> overlappingIds) {
        super("CADASTRAL_OVERLAP", "La parcelle chevauche des parcelles existantes : " + overlappingIds);
    }
}
