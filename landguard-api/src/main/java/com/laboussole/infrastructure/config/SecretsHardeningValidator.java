package com.laboussole.infrastructure.config;

import com.laboussole.infrastructure.security.JwtProperties;
import com.laboussole.infrastructure.storage.StorageEncryptionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

/**
 * Refuses to start on a weak or publicly-known secret.
 *
 * <p>Removing the fallback values from {@code application.yml} already means a
 * misconfigured deployment fails instead of silently booting on a secret that
 * anyone can read in the repository. This validator closes the remaining hole:
 * someone copying a development value into a staging environment variable, or
 * reusing a credential that has already leaked into git history.
 *
 * <p>Denied values are matched by SHA-256 digest, never by plaintext — this
 * class must not become the new place where the secrets live. Digests are
 * one-way, so publishing them here reveals nothing, while still making the
 * exact known-bad values unusable.
 *
 * <p>Runs as an {@link InitializingBean}, i.e. during context refresh: the
 * application never reaches the point of accepting a request.
 */
@Component
class SecretsHardeningValidator implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(SecretsHardeningValidator.class);

    /** HS384 signing keys must carry at least 256 bits of material. */
    private static final int MIN_SECRET_BYTES = 32;

    /**
     * SHA-256 of every value known to be compromised or development-only:
     * the database password and AES key that reached git history, and the
     * throwaway keys shipped in {@code application-dev.yml}.
     */
    private static final Set<String> DENIED_DIGESTS = Set.of(
            "61e22fc7c04800c649ccfe4c75442f95735b349a1b4039c09026d85a82ebd6c2", // leaked DB password
            "61f94a185fec0ecd2b636b95382cbe9eabbc7824e8473260b247cb7eea5eaaed", // leaked AES storage key
            "341a26583a725aed7fc29cb22f52edbad49e94fe04d63a3e7b655197e06aaa04", // dev JWT secret
            "f28afc67cc88d0383d9eedacfcd08a09c684ae4a4882e7afddf28addd224ab77", // dev JWT refresh secret
            "099def9c500c15e067c2e5eacf1adf6367ab56f80c7b93b3f83f51be99e4b94f", // dev AES storage key
            "927db0981f1b28cdcd96589ced8d0218f73e88a0a858f83b7abd7f1583973b9c"  // dev accounts password
    );

    private final Environment environment;
    private final JwtProperties jwtProperties;
    private final StorageEncryptionProperties storageEncryption;

    SecretsHardeningValidator(
            Environment environment,
            JwtProperties jwtProperties,
            StorageEncryptionProperties storageEncryption) {
        this.environment = environment;
        this.jwtProperties = jwtProperties;
        this.storageEncryption = storageEncryption;
    }

    @Override
    public void afterPropertiesSet() {
        if (environment.matchesProfiles("dev")) {
            log.warn("Running on the `dev` profile: throwaway secrets from application-dev.yml are "
                    + "in use. They are rejected on every other profile.");
            return;
        }

        var failures = new ArrayList<String>();

        checkSigningKey("laboussole.security.jwt.secret", jwtProperties.secret(), failures);
        if (jwtProperties.refreshSecret() != null && !jwtProperties.refreshSecret().isBlank()) {
            checkSigningKey("laboussole.security.jwt.refresh-secret",
                    jwtProperties.refreshSecret(), failures);
        } else {
            log.warn("laboussole.security.jwt.refresh-secret is not set: refresh tokens are signed "
                    + "with the access-token key. Set a dedicated key.");
        }

        // A blank key silently stores land titles and identity papers in clear
        // text. Acceptable while developing, never once real documents arrive.
        if (!storageEncryption.isConfigured()) {
            failures.add("laboussole.storage.encryption.key is blank — documents would be stored "
                    + "unencrypted at rest");
        } else {
            checkSigningKey("laboussole.storage.encryption.key", storageEncryption.key(), failures);
        }

        checkNotDenied("spring.datasource.password",
                environment.getProperty("spring.datasource.password"), failures);

        if (!failures.isEmpty()) {
            throw new IllegalStateException(buildMessage(failures));
        }
        log.info("Secret hardening checks passed for profile(s) {}.",
                String.join(",", environment.getActiveProfiles()));
    }

    /** Length and denylist checks for a key that must carry real entropy. */
    private void checkSigningKey(String property, String value, List<String> failures) {
        if (value == null || value.isBlank()) {
            failures.add(property + " is not set");
            return;
        }
        if (materialLength(value) < MIN_SECRET_BYTES) {
            failures.add(property + " carries fewer than " + MIN_SECRET_BYTES
                    + " bytes of key material");
        }
        checkNotDenied(property, value, failures);
    }

    private void checkNotDenied(String property, String value, List<String> failures) {
        if (value == null || value.isBlank()) {
            failures.add(property + " is not set");
            return;
        }
        if (DENIED_DIGESTS.contains(sha256Hex(value))) {
            failures.add(property + " uses a development or already-leaked value");
        }
    }

    /** Byte length once base64-decoded, falling back to the raw bytes. */
    private static int materialLength(String value) {
        try {
            return Base64.getDecoder().decode(value).length;
        } catch (IllegalArgumentException notBase64) {
            return value.getBytes(StandardCharsets.UTF_8).length;
        }
    }

    private static String buildMessage(List<String> failures) {
        return """
                Démarrage interrompu : la configuration des secrets est incomplète ou compromise.

                %s

                Renseignez ces valeurs par variables d'environnement. Générez des clés neuves :
                  JWT_SECRET               : openssl rand -base64 48
                  JWT_REFRESH_SECRET       : openssl rand -base64 48
                  STORAGE_ENCRYPTION_KEY   : openssl rand -base64 32

                Aucune valeur de développement ne peut être réutilisée hors du profil `dev`."""
                .formatted(failures.stream()
                        .map(failure -> "  - " + failure)
                        .reduce((a, b) -> a + "\n" + b)
                        .orElse(""));
    }

    private static String sha256Hex(String value) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
