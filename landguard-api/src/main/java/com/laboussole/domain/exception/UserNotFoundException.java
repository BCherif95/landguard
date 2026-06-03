package com.laboussole.domain.exception;

import com.laboussole.domain.model.UserId;

public final class UserNotFoundException extends DomainException {

    public UserNotFoundException(UserId id) {
        super("USER_NOT_FOUND", "User not found: " + id.asString());
    }
}
