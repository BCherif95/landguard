package com.laboussole.domain.port.out;

import com.laboussole.domain.model.HashedPassword;

/** Driven port: cryptographic hashing of plaintext passwords. */
public interface PasswordHasher {

    HashedPassword hash(String plaintext);

    boolean matches(String plaintext, HashedPassword hashed);
}
