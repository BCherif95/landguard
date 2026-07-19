package com.laboussole.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * At-rest encryption of stored documents (PRD 2.1: AES-256 for titles and
 * plans). {@code key} is a base64-encoded 256-bit AES key, always injected
 * from the environment in non-dev profiles. A blank key disables encryption
 * (loudly logged) so local tooling can inspect files if ever needed.
 */
@ConfigurationProperties(prefix = "laboussole.storage.encryption")
public record StorageEncryptionProperties(String key) {

    public boolean isConfigured() {
        return key != null && !key.isBlank();
    }
}
