package com.laboussole.application.usecase;

import com.laboussole.domain.exception.CadastralReferenceTakenException;
import com.laboussole.domain.model.parcel.CadastralReference;
import com.laboussole.domain.model.parcel.Hectares;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.LandDocument;
import com.laboussole.domain.model.parcel.MoneyXof;
import com.laboussole.domain.port.in.RegisterLandParcelUseCase;
import com.laboussole.domain.port.out.LandParcelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterLandParcelService implements RegisterLandParcelUseCase {

    private final LandParcelRepository repository;

    public RegisterLandParcelService(LandParcelRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public LandParcel execute(Command command) {
        var reference = CadastralReference.of(command.reference());
        if (repository.existsByReference(reference)) {
            throw new CadastralReferenceTakenException(reference);
        }

        var overlapping = repository.findOverlappingParcels(command.geometry());
        if (!overlapping.isEmpty()) {
            var ids = overlapping.stream().map(com.laboussole.domain.model.parcel.LandParcel::id).toList();
            throw new com.laboussole.domain.exception.CadastralOverlapException(ids);
        }

        var documents = command.documents().stream()
                .map(d -> LandDocument.create(d.type(), d.label(), d.storageKey(), d.fileName(), "application/pdf"))
                .toList();

        var parcel = LandParcel.register(
                reference,
                command.name(),
                command.regionLabel(),
                command.ownerLabel(),
                command.ownerUserId(),
                Hectares.of(command.areaHectares()),
                MoneyXof.of(command.estimatedValueXof()),
                command.geometry(),
                documents);
        return repository.save(parcel);
    }
}
