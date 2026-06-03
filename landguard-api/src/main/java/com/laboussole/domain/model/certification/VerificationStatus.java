package com.laboussole.domain.model.certification;

/**
 * Realistic Malian Land Certification Statuses
 */
public enum VerificationStatus {
    DRAFT,                      // BROUILLON
    PENDING_VERIFICATION,       // EN_ATTENTE_VERIFICATION
    DOMAIN_CONTROL,             // EN_CONTROLE_DOMANIAL
    TF_VERIFIED,                // TF_VERIFIE
    TF_REJECTED,                // TF_REJETE
    DISPUTE_SIGNALED,           // LITIGE_SIGNALE
    PENDING_COMPLEMENT,         // EN_ATTENTE_COMPLEMENT
    CERTIFIED;                  // CERTIFIE

    public boolean canTransitionTo(VerificationStatus target) {
        return switch (this) {
            case DRAFT -> target == PENDING_VERIFICATION || target == TF_REJECTED;
            case PENDING_VERIFICATION -> target == DOMAIN_CONTROL || target == PENDING_COMPLEMENT || target == TF_REJECTED;
            case DOMAIN_CONTROL -> target == TF_VERIFIED || target == DISPUTE_SIGNALED || target == PENDING_COMPLEMENT || target == TF_REJECTED;
            case TF_VERIFIED -> target == CERTIFIED || target == TF_REJECTED;
            case PENDING_COMPLEMENT -> target == PENDING_VERIFICATION || target == TF_REJECTED;
            case CERTIFIED, TF_REJECTED, DISPUTE_SIGNALED -> false;
        };
    }
}
