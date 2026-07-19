package com.laboussole.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * First-run administrator account, supplied through the environment
 * (BOOTSTRAP_ADMIN_EMAIL / BOOTSTRAP_ADMIN_PASSWORD / BOOTSTRAP_ADMIN_FULL_NAME).
 * Replaces the demo-user seed migrations: no credentials live in the codebase.
 */
@ConfigurationProperties(prefix = "laboussole.bootstrap.admin")
public record BootstrapAdminProperties(String email, String password, String fullName) {

    public boolean isConfigured() {
        return email != null && !email.isBlank()
                && password != null && !password.isBlank();
    }
}
