package com.laboussole.domain.port.in;

import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.parcel.LandParcel;

import java.util.List;
import java.util.Optional;

public interface ListLandParcelsUseCase {

    List<LandParcel> execute(Query query);

    /** {@code ownerUserId} optional: when present, restricts to that owner's parcels. */
    record Query(Optional<UserId> ownerUserId, int limit) {

        public Query {
            if (limit <= 0 || limit > 500) {
                throw new IllegalArgumentException("limit must be in (0, 500]");
            }
        }
    }
}
