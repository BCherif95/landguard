package com.laboussole.infrastructure.security;

import com.laboussole.domain.model.Role;
import com.laboussole.domain.model.UserId;

/**
 * Spring Security principal — kept minimal so the controllers depend on a stable shape
 * rather than on the full {@code User} aggregate (which lives in domain).
 */
public record AuthenticatedPrincipal(UserId userId, Role role) {

    public boolean hasAnyRole(Role... roles) {
        for (Role candidate : roles) {
            if (role == candidate) {
                return true;
            }
        }
        return false;
    }
}
