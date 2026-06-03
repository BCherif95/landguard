package com.laboussole.domain.model.heritage;

import com.laboussole.domain.model.parcel.ParcelId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class SuccessionPlan {
    private final SuccessionPlanId id;
    private final ParcelId parcelId;
    private final List<Heir> heirs;
    private SuccessionStatus status;
    private String blockchainHash;
    private final Instant createdAt;
    private Instant updatedAt;

    public SuccessionPlan(
            SuccessionPlanId id,
            ParcelId parcelId,
            List<Heir> heirs,
            SuccessionStatus status,
            String blockchainHash,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.parcelId = Objects.requireNonNull(parcelId);
        this.heirs = new ArrayList<>(Objects.requireNonNull(heirs));
        this.status = Objects.requireNonNull(status);
        this.blockchainHash = blockchainHash;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        validateTotalShares();
    }

    public static SuccessionPlan create(ParcelId parcelId) {
        var now = Instant.now();
        return new SuccessionPlan(
                SuccessionPlanId.generate(),
                parcelId,
                new ArrayList<>(),
                SuccessionStatus.DRAFT,
                null,
                now,
                now
        );
    }

    public void addHeir(Heir heir) {
        if (status != SuccessionStatus.DRAFT) {
            throw new IllegalStateException("Can only add heirs to a DRAFT plan");
        }
        this.heirs.add(heir);
        validateTotalShares();
        this.updatedAt = Instant.now();
    }

    public void submitForVoting() {
        if (heirs.isEmpty()) {
            throw new IllegalStateException("Succession plan must have at least one heir");
        }
        if (status != SuccessionStatus.DRAFT) {
            throw new IllegalStateException("Can only submit DRAFT plans for voting");
        }
        this.status = SuccessionStatus.IN_VOTING;
        this.updatedAt = Instant.now();
    }

    public void castVote(HeirId heirId, boolean approved) {
        if (status != SuccessionStatus.IN_VOTING) {
            throw new IllegalStateException("Can only vote on plans IN_VOTING");
        }
        
        heirs.stream()
                .filter(h -> h.id().equals(heirId))
                .findFirst()
                .ifPresent(heir -> {
                    if (approved) {
                        heir.validate();
                    } else {
                        this.status = SuccessionStatus.REJECTED;
                    }
                });
        
        if (status != SuccessionStatus.REJECTED && heirs.stream().allMatch(Heir::validated)) {
            this.status = SuccessionStatus.VALIDATED;
        }
        this.updatedAt = Instant.now();
    }

    public void anchorOnBlockchain(String hash) {
        if (status != SuccessionStatus.VALIDATED) {
            throw new IllegalStateException("Can only anchor VALIDATED plans");
        }
        this.blockchainHash = Objects.requireNonNull(hash);
        this.status = SuccessionStatus.ANCHORED;
        this.updatedAt = Instant.now();
    }

    private void validateTotalShares() {
        int total = heirs.stream().mapToInt(Heir::sharePercentage).sum();
        if (total > 100) {
            throw new IllegalStateException("Total shares cannot exceed 100%");
        }
    }

    public void markAsTransferred() {
        if (status != SuccessionStatus.ANCHORED) {
            throw new IllegalStateException("Can only transfer ownership for ANCHORED plans");
        }
        this.status = SuccessionStatus.TRANSFERRED;
        this.updatedAt = Instant.now();
    }

    public SuccessionPlanId id() { return id; }
    public ParcelId parcelId() { return parcelId; }
    public List<Heir> heirs() { return Collections.unmodifiableList(heirs); }
    public SuccessionStatus status() { return status; }
    public String blockchainHash() { return blockchainHash; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
