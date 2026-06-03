package com.laboussole.domain.exception;

import com.laboussole.domain.model.Email;

public final class EmailAlreadyRegisteredException extends DomainException {

    public EmailAlreadyRegisteredException(Email email) {
        super("EMAIL_ALREADY_REGISTERED", "Email already registered: " + email.value());
    }
}
