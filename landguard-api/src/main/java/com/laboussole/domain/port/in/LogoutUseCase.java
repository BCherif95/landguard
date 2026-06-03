package com.laboussole.domain.port.in;

/** Driving port: revoke a refresh token. Idempotent — unknown tokens are silently ignored. */
public interface LogoutUseCase {

    void execute(Command command);

    record Command(String refreshToken) {}
}
