package com.laboussole.domain.model.parcel;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A land document sealed at registration time: {@code sha256Hash} is the
 * hex-encoded SHA-256 fingerprint of the stored file (PRD 2.1). Any later
 * modification of the file no longer matches the fingerprint, which breaks
 * the validation chain.
 */
public final class LandDocument {
    private final UUID id;
    private final String type; // TF, PLAN, ID, CESSION
    private final String label;
    private final String storageKey;
    private final String fileName;
    private final String contentType;
    private final String sha256Hash;
    private final Instant uploadedAt;

    public LandDocument(UUID id, String type, String label, String storageKey, String fileName,
                        String contentType, String sha256Hash, Instant uploadedAt) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.label = Objects.requireNonNull(label);
        this.storageKey = Objects.requireNonNull(storageKey);
        this.fileName = Objects.requireNonNull(fileName);
        this.contentType = Objects.requireNonNull(contentType);
        this.sha256Hash = sha256Hash;
        this.uploadedAt = Objects.requireNonNull(uploadedAt);
    }

    public static LandDocument create(String type, String label, String storageKey, String fileName,
                                      String contentType, String sha256Hash) {
        return new LandDocument(UUID.randomUUID(), type, label, storageKey, fileName, contentType,
                sha256Hash, Instant.now());
    }

    public UUID id() { return id; }
    public String type() { return type; }
    public String label() { return label; }
    public String storageKey() { return storageKey; }
    public String fileName() { return fileName; }
    public String contentType() { return contentType; }
    public String sha256Hash() { return sha256Hash; }
    public Instant uploadedAt() { return uploadedAt; }
}
