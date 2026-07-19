package com.laboussole.application.usecase;

import com.laboussole.domain.exception.CadastralReferenceTakenException;
import com.laboussole.domain.model.parcel.CadastralReference;
import com.laboussole.domain.model.parcel.Hectares;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.LandDocument;
import com.laboussole.domain.model.parcel.MoneyXof;
import com.laboussole.application.service.Sha256;
import com.laboussole.domain.port.in.RegisterLandParcelUseCase;
import com.laboussole.domain.port.out.LandParcelRepository;
import com.laboussole.domain.port.out.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

@Service
public class RegisterLandParcelService implements RegisterLandParcelUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterLandParcelService.class);

    private final LandParcelRepository repository;
    private final StorageService storage;

    public RegisterLandParcelService(LandParcelRepository repository, StorageService storage) {
        this.repository = repository;
        this.storage = storage;
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

        // Each document is sealed with the SHA-256 fingerprint of the stored
        // file (PRD 2.1): the hash is computed server-side from storage, never
        // taken from the client.
        var documents = command.documents().stream()
                .map(d -> LandDocument.create(d.type(), d.label(), d.storageKey(), d.fileName(),
                        "application/pdf", fingerprintOf(d.storageKey())))
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

    private String fingerprintOf(String storageKey) {
        try (InputStream in = storage.load(storageKey)) {
            return Sha256.hex(in);
        } catch (Exception e) {
            // A registration must not be lost because a fingerprint could not be
            // computed; the missing seal stays visible (null hash) for follow-up.
            log.error("Could not fingerprint document {}: {}", storageKey, e.getMessage());
            return null;
        }
    }
}
