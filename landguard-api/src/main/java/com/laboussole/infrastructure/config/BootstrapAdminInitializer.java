package com.laboussole.infrastructure.config;

import com.laboussole.domain.model.Email;
import com.laboussole.domain.model.Role;
import com.laboussole.domain.model.User;
import com.laboussole.domain.port.out.PasswordHasher;
import com.laboussole.domain.port.out.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the initial ADMIN account on startup when the environment provides
 * one and no account with that e-mail exists yet. Idempotent: a subsequent
 * start with the same configuration is a no-op.
 */
@Component
public class BootstrapAdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminInitializer.class);

    private final BootstrapAdminProperties properties;
    private final UserRepository users;
    private final PasswordHasher passwordHasher;

    public BootstrapAdminInitializer(
            BootstrapAdminProperties properties,
            UserRepository users,
            PasswordHasher passwordHasher) {
        this.properties = properties;
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.isConfigured()) {
            log.info("No bootstrap admin configured (set BOOTSTRAP_ADMIN_EMAIL / "
                    + "BOOTSTRAP_ADMIN_PASSWORD to create one on first start).");
            return;
        }
        var email = Email.of(properties.email());
        if (users.existsByEmail(email)) {
            return;
        }
        var fullName = properties.fullName() == null || properties.fullName().isBlank()
                ? "Administrateur"
                : properties.fullName();
        users.save(User.register(email, fullName, passwordHasher.hash(properties.password()), Role.ADMIN));
        log.info("Bootstrap admin account created for {}", properties.email());
    }
}
