-- LA BOUSSOLE — rotating refresh tokens with reuse-detection chain.
-- MySQL version.

CREATE TABLE refresh_tokens (
    id              VARCHAR(36)  PRIMARY KEY,
    user_id         VARCHAR(36)  NOT NULL,
    token_hash      VARCHAR(128) NOT NULL,
    issued_at       DATETIME(6)  NOT NULL,
    expires_at      DATETIME(6)  NOT NULL,
    revoked_at      DATETIME(6),
    replaced_by     VARCHAR(36),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_refresh_tokens_replaced FOREIGN KEY (replaced_by) REFERENCES refresh_tokens (id) ON DELETE SET NULL
);

CREATE UNIQUE INDEX ux_refresh_tokens_hash    ON refresh_tokens (token_hash);
CREATE INDEX        ix_refresh_tokens_user    ON refresh_tokens (user_id);
CREATE INDEX        ix_refresh_tokens_expires ON refresh_tokens (expires_at);
