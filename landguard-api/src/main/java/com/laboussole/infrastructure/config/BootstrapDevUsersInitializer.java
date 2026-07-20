package com.laboussole.infrastructure.config;

import com.laboussole.domain.model.Email;
import com.laboussole.domain.model.Role;
import com.laboussole.domain.model.User;
import com.laboussole.domain.port.out.PasswordHasher;
import com.laboussole.domain.port.out.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds one ready-to-use account per {@link Role} on startup, for local
 * development only. Guarded by {@code @Profile("dev")} so it never runs in
 * staging or production, where accounts come from {@link BootstrapAdminInitializer}
 * (environment-supplied credentials). Idempotent: existing e-mails are left untouched.
 *
 * <p>Disable with {@code laboussole.bootstrap.dev-users.enabled=false}; override the
 * shared password with {@code laboussole.bootstrap.dev-users.password}.
 */
@Component
@Profile("dev")
public class BootstrapDevUsersInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapDevUsersInitializer.class);

    /** One demo account per RBAC role. E-mails in English, display names in French. */
    private static final DevUser[] DEV_USERS = {
            new DevUser("admin@laboussole.ml", "Administrateur Démo", Role.ADMIN),
            new DevUser("officer@laboussole.ml", "Agent Cadastral Démo", Role.OFFICER),
            new DevUser("legal@laboussole.ml", "Notaire Démo", Role.LEGAL),
            new DevUser("banker@laboussole.ml", "Banque Démo", Role.BANKER),
            new DevUser("citizen@laboussole.ml", "Citoyen Démo", Role.CITIZEN),
    };

    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final boolean enabled;
    private final String password;

    public BootstrapDevUsersInitializer(
            UserRepository users,
            PasswordHasher passwordHasher,
            @Value("${laboussole.bootstrap.dev-users.enabled:true}") boolean enabled,
            @Value("${laboussole.bootstrap.dev-users.password:Boussole2026!}") String password) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.enabled = enabled;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("Dev users seeding disabled (laboussole.bootstrap.dev-users.enabled=false).");
            return;
        }

        int created = 0;
        for (DevUser devUser : DEV_USERS) {
            var email = Email.of(devUser.email());
            if (users.existsByEmail(email)) {
                continue;
            }
            users.save(User.register(
                    email,
                    devUser.fullName(),
                    passwordHasher.hash(password),
                    devUser.role()));
            created++;
            log.info("Dev account seeded: {} ({})", devUser.email(), devUser.role());
        }

        if (created > 0) {
            log.warn("Seeded {} dev account(s). Shared password: \"{}\". "
                    + "DEV PROFILE ONLY — never enabled in production.", created, password);
        }
    }

    private record DevUser(String email, String fullName, Role role) {
    }
}
