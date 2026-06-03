package com.laboussole.domain.exception;

public final class InvalidRefreshTokenException extends DomainException {

    public InvalidRefreshTokenException(String reason) {
        super("INVALID_REFRESH_TOKEN", "Refresh token rejected: " + reason);
    }
}
