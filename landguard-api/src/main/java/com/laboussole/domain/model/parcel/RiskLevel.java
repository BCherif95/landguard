package com.laboussole.domain.model.parcel;

/** Discretised fraud / disturbance risk level derived from the {@code riskScore} (0..100). */
public enum RiskLevel {
    LOW, MEDIUM, HIGH, CRITICAL;

    public static RiskLevel fromScore(int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Risk score must be between 0 and 100");
        }
        if (score < 25) return LOW;
        if (score < 55) return MEDIUM;
        if (score < 80) return HIGH;
        return CRITICAL;
    }
}
