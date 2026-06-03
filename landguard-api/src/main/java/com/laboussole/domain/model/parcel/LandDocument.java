package com.laboussole.domain.model.parcel;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class LandDocument {
    private final UUID id;
    private final String type; // TF, PLAN, ID, CESSION
    private final String label;
    private final String storageKey;
    private final String fileName;
    private final String contentType;
    private final Instant uploadedAt;

    public LandDocument(UUID id, String type, String label, String storageKey, String fileName, String contentType, Instant uploadedAt) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.label = Objects.requireNonNull(label);
        this.storageKey = Objects.requireNonNull(storageKey);
        this.fileName = Objects.requireNonNull(fileName);
        this.contentType = Objects.requireNonNull(contentType);
        this.uploadedAt = Objects.requireNonNull(uploadedAt);
    }

    public static LandDocument create(String type, String label, String storageKey, String fileName, String contentType) {
        return new LandDocument(UUID.randomUUID(), type, label, storageKey, fileName, contentType, Instant.now());
    }

    public UUID id() { return id; }
    public String type() { return type; }
    public String label() { return label; }
    public String storageKey() { return storageKey; }
    public String fileName() { return fileName; }
    public String contentType() { return contentType; }
    public Instant uploadedAt() { return uploadedAt; }
}
