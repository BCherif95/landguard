package com.laboussole.domain.exception;

import com.laboussole.domain.model.parcel.CadastralReference;

public final class CadastralReferenceTakenException extends DomainException {

    public CadastralReferenceTakenException(CadastralReference reference) {
        super("CADASTRAL_REFERENCE_TAKEN",
                "Cadastral reference already registered: " + reference.value());
    }
}
