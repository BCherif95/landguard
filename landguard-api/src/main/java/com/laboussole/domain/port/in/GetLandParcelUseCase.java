package com.laboussole.domain.port.in;

import com.laboussole.domain.model.Role;
import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.ParcelId;

import java.util.Objects;

public interface GetLandParcelUseCase {

    /**
     * Returns the parcel if the requester is allowed to see it. When a requester
     * without registry-wide access asks for a parcel they do not own, the service
     * throws {@code ParcelNotFoundException} (mapped to 404) rather than 403, so
     * the existence of the parcel is not revealed.
     */
    LandParcel execute(Query query);

    record Query(ParcelId parcelId, UserId requesterId, Role requesterRole) {

        public Query {
            Objects.requireNonNull(parcelId, "parcelId must not be null");
            Objects.requireNonNull(requesterId, "requesterId must not be null");
            Objects.requireNonNull(requesterRole, "requesterRole must not be null");
        }
    }
}
