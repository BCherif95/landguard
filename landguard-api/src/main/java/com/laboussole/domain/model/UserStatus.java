package com.laboussole.domain.model;

/** Lifecycle status of a user account. */
public enum UserStatus {
    /** Newly registered, may be active immediately or pending email confirmation. */
    PENDING,
    /** Allowed to authenticate. */
    ACTIVE,
    /** Soft-disabled by an admin or by repeated security violations. */
    DISABLED,
    /** Locked out (e.g. too many failed logins). May auto-recover. */
    LOCKED;

    public boolean canAuthenticate() {
        return this == ACTIVE;
    }
}
