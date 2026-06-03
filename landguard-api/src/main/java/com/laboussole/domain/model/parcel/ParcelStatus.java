package com.laboussole.domain.model.parcel;

/** Lifecycle of a registered land parcel in the Malian land management system. */
public enum ParcelStatus {
    /** Initial draft, not yet submitted. */
    DRAFT,
    /** Submitted by the owner/notary, awaiting initial review. */
    SUBMITTED,
    /** Technical survey in progress (field work). */
    UNDER_SURVEY,
    /** Administrative verification of documents and rights. */
    UNDER_VERIFICATION,
    /** Review by a notary for legal compliance. */
    UNDER_NOTARY_REVIEW,
    /** Final administrative review before certification. */
    UNDER_ADMIN_REVIEW,
    /** Geometrically and legally certified. */
    CERTIFIED,
    /** Land Title (Titre Foncier) has been officially issued. */
    TITLE_ISSUED,
    /** Active dispute (boundaries, ownership, inheritance). */
    DISPUTED,
    /** Application rejected. */
    REJECTED,
    /** Archived or cancelled. */
    ARCHIVED;

    public boolean canTransitionTo(ParcelStatus target) {
        if (this == target) return true;
        
        return switch (this) {
            case DRAFT -> target == SUBMITTED || target == ARCHIVED;
            case SUBMITTED -> target == UNDER_SURVEY || target == UNDER_VERIFICATION || target == REJECTED;
            case UNDER_SURVEY -> target == UNDER_VERIFICATION || target == DISPUTED || target == REJECTED;
            case UNDER_VERIFICATION -> target == UNDER_NOTARY_REVIEW || target == DISPUTED || target == REJECTED;
            case UNDER_NOTARY_REVIEW -> target == UNDER_ADMIN_REVIEW || target == DISPUTED || target == REJECTED;
            case UNDER_ADMIN_REVIEW -> target == CERTIFIED || target == DISPUTED || target == REJECTED;
            case CERTIFIED -> target == TITLE_ISSUED || target == DISPUTED;
            case TITLE_ISSUED -> target == DISPUTED || target == ARCHIVED;
            case DISPUTED -> target == UNDER_VERIFICATION || target == ARCHIVED;
            case REJECTED -> target == DRAFT || target == ARCHIVED;
            case ARCHIVED -> false;
        };
    }
}
