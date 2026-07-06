package com.laboussole.domain.model.parcel;

/**
 * Kinds of land documents accepted by the registry. Mirrors the labels used
 * by {@link LandDocument#type()} (kept as free text there for backward
 * compatibility with persisted rows).
 */
public enum DocumentType {
    /** Titre Foncier — the official land title. */
    TF,
    /** Plan de bornage / plan de situation. */
    PLAN,
    /** National identity document (NINA). */
    ID,
    /** Acte de cession. */
    CESSION,
    /** Tax receipt. */
    TAX,
    /** Fallback when the caller did not specify a type. */
    UNKNOWN;

    public static DocumentType fromLabel(String label) {
        if (label == null || label.isBlank()) {
            return UNKNOWN;
        }
        try {
            return DocumentType.valueOf(label.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
