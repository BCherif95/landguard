package com.laboussole.domain.port.in;

import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.ParcelGeometry;

import java.math.BigDecimal;
import java.util.List;

public interface RegisterLandParcelUseCase {

    LandParcel execute(Command command);

    record Command(
            String reference,
            String name,
            String regionLabel,
            String ownerLabel,
            UserId ownerUserId,
            BigDecimal areaHectares,
            long estimatedValueXof,
            ParcelGeometry geometry,
            List<DocumentCommand> documents) {}

    record DocumentCommand(
            String type,
            String label,
            String storageKey,
            String fileName) {}
}
