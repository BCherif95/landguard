package com.laboussole.infrastructure.security;

import org.springframework.util.AntPathMatcher;

/**
 * Single source of truth for endpoints reachable without authentication, shared by
 * the security filter chain and the JWT filter (which must not reject a stale
 * Authorization header sent to a public endpoint such as /auth/refresh).
 */
final class PublicEndpoints {

    static final String[] PATTERNS = {
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/api/v1/monitoring/stream",
            "/api/v1/monitoring/snapshots/image/*",
            "/api/v1/public/**",
            "/actuator/health",
            "/actuator/info",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    static boolean matches(String path) {
        for (String pattern : PATTERNS) {
            if (MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private PublicEndpoints() {
    }
}
