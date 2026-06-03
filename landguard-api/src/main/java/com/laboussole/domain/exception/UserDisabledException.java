package com.laboussole.domain.exception;

public final class UserDisabledException extends DomainException {

    public UserDisabledException() {
        super("USER_DISABLED", "User account is not active");
    }
}
