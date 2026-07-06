package com.laboussole.application.usecase;

import com.laboussole.domain.port.in.LogoutUseCase;
import org.springframework.stereotype.Service;

/**
 * With stateless refresh tokens there is nothing to revoke server-side: logout is
 * effective the moment the client discards its tokens (handled by the frontend
 * auth store). This endpoint is kept as an explicit, idempotent confirmation so
 * the API contract remains stable for existing clients.
 */
@Service
public class LogoutService implements LogoutUseCase {

    @Override
    public void execute(Command command) {
        // Intentionally empty: token invalidation happens client-side only.
        // See the class Javadoc for the stateless-logout contract.
    }
}
