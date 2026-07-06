package com.laboussole.domain.port.in;

import com.laboussole.domain.model.Role;
import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.parcel.LandParcel;

import java.util.List;
import java.util.Objects;

public interface ListLandParcelsUseCase {

    List<LandParcel> execute(Query query);

    /**
     * Visibility is derived from the authenticated requester, never from a
     * client-supplied filter: the service decides what the requester may see
     * based on {@code requesterRole}.
     */
    record Query(UserId requesterId, Role requesterRole, int limit) {

        public Query {
            Objects.requireNonNull(requesterId, "requesterId must not be null");
            Objects.requireNonNull(requesterRole, "requesterRole must not be null");
            if (limit <= 0 || limit > 500) {
                throw new IllegalArgumentException("limit must be in (0, 500]");
            }
        }
    }
}
