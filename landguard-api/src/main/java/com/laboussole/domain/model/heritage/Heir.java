package com.laboussole.domain.model.heritage;

import com.laboussole.domain.model.UserId;

import java.util.Objects;

/**
 * An heir recorded on a succession plan. When the heir owns a platform account,
 * {@code linkedUserId} associates it (Feature 04.1): linked accounts receive
 * every surveillance alert and status-change notification of the parcel
 * (Feature 04.2), so no single member can act in secret.
 */
public final class Heir {
    private final HeirId id;
    private final String fullName;
    private final String relation;
    private final int sharePercentage;
    private boolean validated;
    private final UserId linkedUserId;

    public Heir(HeirId id, String fullName, String relation, int sharePercentage,
                boolean validated, UserId linkedUserId) {
        this.id = Objects.requireNonNull(id);
        this.fullName = Objects.requireNonNull(fullName);
        this.relation = Objects.requireNonNull(relation);
        this.sharePercentage = clampShare(sharePercentage);
        this.validated = validated;
        this.linkedUserId = linkedUserId;
    }

    public static Heir create(String fullName, String relation, int sharePercentage,
                              UserId linkedUserId) {
        return new Heir(HeirId.generate(), fullName, relation, sharePercentage, false, linkedUserId);
    }

    public void validate() {
        this.validated = true;
    }

    private int clampShare(int share) {
        if (share < 0 || share > 100) {
            throw new IllegalArgumentException("Share percentage must be between 0 and 100");
        }
        return share;
    }

    public HeirId id() { return id; }
    public String fullName() { return fullName; }
    public String relation() { return relation; }
    public int sharePercentage() { return sharePercentage; }
    public boolean validated() { return validated; }
    public UserId linkedUserId() { return linkedUserId; }
}
