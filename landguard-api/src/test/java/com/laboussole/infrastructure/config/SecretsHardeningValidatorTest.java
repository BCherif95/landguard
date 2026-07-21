package com.laboussole.infrastructure.config;

import com.laboussole.infrastructure.security.JwtProperties;
import com.laboussole.infrastructure.storage.StorageEncryptionProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecretsHardeningValidatorTest {

    /** Values shipped in application-dev.yml — must be refused elsewhere. */
    private static final String DEV_JWT_SECRET =
            "ZGV2LW9ubHktc2VjcmV0LWtleS1kby1ub3QtdXNlLWluLXByb2R1Y3Rpb24tcGxlYXNl";
    private static final String DEV_STORAGE_KEY = "ZGV2LW9ubHktYWVzLWtleS1uZXZlci1pbi1wcm9kISE=";

    /** The AES key that reached git history in commit 5ef1061. */
    private static final String LEAKED_STORAGE_KEY = "41ZfeQFM3H8pmwc84DC6Fr1adlVtxWEZzaW+4mATJiY=";

    /** Freshly generated, 48 and 32 bytes — what a real deployment supplies. */
    private static final String STRONG_JWT_SECRET =
            "T2ggUzhkVXJZcm1RRnZ4S3AzTndhWmpMYzdIdEJlRzVpUXNWZE5rWXhQMkE0Umh6";
    private static final String STRONG_REFRESH_SECRET =
            "WjRtVHhLOXBSc0Z2Q2gyTndkWWpMYjdIdEJlRzVpUXNWZE5rWXhQMkE0UmhhUXc=";
    private static final String STRONG_STORAGE_KEY = "9pQ3vLxK7mNsTfBhRjWzYcD2aE5gU8iX4oZrVnQwJk0=";

    private static SecretsHardeningValidator validator(
            MockEnvironment environment, String jwtSecret, String refreshSecret, String storageKey) {
        return new SecretsHardeningValidator(
                environment,
                new JwtProperties("laboussole-api", jwtSecret, refreshSecret,
                        Duration.ofMinutes(15), Duration.ofDays(30)),
                new StorageEncryptionProperties(storageKey));
    }

    private static MockEnvironment production(String datasourcePassword) {
        var environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        environment.setProperty("spring.datasource.password", datasourcePassword);
        return environment;
    }

    @Test
    @DisplayName("the dev profile is exempt — throwaway secrets are expected there")
    void devProfileIsExempt() {
        var environment = new MockEnvironment();
        environment.setActiveProfiles("dev");

        assertThatCode(() -> validator(environment, DEV_JWT_SECRET, null, DEV_STORAGE_KEY)
                .afterPropertiesSet())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("a fully configured production profile starts")
    void strongProductionConfigurationPasses() {
        assertThatCode(() -> validator(
                production("a-real-database-password"),
                STRONG_JWT_SECRET, STRONG_REFRESH_SECRET, STRONG_STORAGE_KEY)
                .afterPropertiesSet())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("the development JWT secret is refused outside dev")
    void devJwtSecretIsRefusedInProduction() {
        assertThatThrownBy(() -> validator(
                production("a-real-database-password"),
                DEV_JWT_SECRET, STRONG_REFRESH_SECRET, STRONG_STORAGE_KEY)
                .afterPropertiesSet())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("laboussole.security.jwt.secret")
                .hasMessageContaining("development or already-leaked value");
    }

    @Test
    @DisplayName("the AES key that leaked into git history is refused")
    void leakedStorageKeyIsRefused() {
        assertThatThrownBy(() -> validator(
                production("a-real-database-password"),
                STRONG_JWT_SECRET, STRONG_REFRESH_SECRET, LEAKED_STORAGE_KEY)
                .afterPropertiesSet())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("laboussole.storage.encryption.key");
    }

    @Test
    @DisplayName("the database password that leaked into git history is refused")
    void leakedDatabasePasswordIsRefused() {
        assertThatThrownBy(() -> validator(
                production("BCherif9085"),
                STRONG_JWT_SECRET, STRONG_REFRESH_SECRET, STRONG_STORAGE_KEY)
                .afterPropertiesSet())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spring.datasource.password");
    }

    @Test
    @DisplayName("a blank storage key is refused — documents would sit in clear text")
    void blankStorageKeyIsRefused() {
        assertThatThrownBy(() -> validator(
                production("a-real-database-password"),
                STRONG_JWT_SECRET, STRONG_REFRESH_SECRET, "")
                .afterPropertiesSet())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("documents would be stored unencrypted");
    }

    @Test
    @DisplayName("a short signing key is refused even when it is not on the denylist")
    void shortSigningKeyIsRefused() {
        assertThatThrownBy(() -> validator(
                production("a-real-database-password"),
                "dGlueQ==", STRONG_REFRESH_SECRET, STRONG_STORAGE_KEY)
                .afterPropertiesSet())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fewer than 32 bytes");
    }

    @Test
    @DisplayName("an omitted refresh secret is tolerated — it falls back to the access key")
    void missingRefreshSecretIsTolerated() {
        assertThatCode(() -> validator(
                production("a-real-database-password"),
                STRONG_JWT_SECRET, null, STRONG_STORAGE_KEY)
                .afterPropertiesSet())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("every failure is reported at once, in French, with the generation commands")
    void allFailuresAreReportedTogether() {
        assertThatThrownBy(() -> validator(
                production("BCherif9085"), DEV_JWT_SECRET, null, "")
                .afterPropertiesSet())
                .isInstanceOf(IllegalStateException.class)
                .satisfies(failure -> assertThat(failure.getMessage())
                        .contains("Démarrage interrompu")
                        .contains("laboussole.security.jwt.secret")
                        .contains("laboussole.storage.encryption.key")
                        .contains("spring.datasource.password")
                        .contains("openssl rand -base64 32"));
    }
}
