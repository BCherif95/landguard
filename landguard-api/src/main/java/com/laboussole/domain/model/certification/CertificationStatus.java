package com.laboussole.domain.model.certification;

public enum CertificationStatus {
    DRAFT,
    ANALYZING,
    VERIFYING,
    LEGAL_CONTROL,
    PENDING_APPROVAL,
    CERTIFIED,
    REJECTED,
    DISPUTE_DETECTED;

    public boolean canTransitionTo(CertificationStatus target) {
        return switch (this) {
            case DRAFT -> target == ANALYZING || target == REJECTED;
            case ANALYZING -> target == VERIFYING || target == REJECTED || target == DISPUTE_DETECTED;
            case VERIFYING -> target == LEGAL_CONTROL || target == REJECTED || target == DISPUTE_DETECTED;
            case LEGAL_CONTROL -> target == PENDING_APPROVAL || target == REJECTED || target == DISPUTE_DETECTED;
            case PENDING_APPROVAL -> target == CERTIFIED || target == REJECTED;
            case CERTIFIED, REJECTED, DISPUTE_DETECTED -> false;
        };
    }
}
