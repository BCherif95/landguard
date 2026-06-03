package com.laboussole.domain.exception;

/** Base class for all domain-level exceptions. Carries a stable error code for translation in the REST layer. */
public abstract class DomainException extends RuntimeException {

    private final String code;

    protected DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
