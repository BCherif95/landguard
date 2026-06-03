package com.laboussole.infrastructure.security;

import com.laboussole.domain.model.HashedPassword;
import com.laboussole.domain.port.out.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class BCryptPasswordHasher implements PasswordHasher {

    private final PasswordEncoder encoder;

    BCryptPasswordHasher(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public HashedPassword hash(String plaintext) {
        if (plaintext == null || plaintext.length() < 10) {
            throw new IllegalArgumentException("Password must be at least 10 characters");
        }
        return new HashedPassword(encoder.encode(plaintext));
    }

    @Override
    public boolean matches(String plaintext, HashedPassword hashed) {
        if (plaintext == null) return false;
        return encoder.matches(plaintext, hashed.value());
    }
}
