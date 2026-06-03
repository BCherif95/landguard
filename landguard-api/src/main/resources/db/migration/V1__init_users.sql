-- LA BOUSSOLE — initial users table.
-- MySQL version.

CREATE TABLE users (
    id              VARCHAR(36)  PRIMARY KEY,
    email           VARCHAR(254) NOT NULL,
    full_name       VARCHAR(200) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(32)  NOT NULL,
    status          VARCHAR(32)  NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    last_login_at   DATETIME(6)
);

CREATE UNIQUE INDEX ux_users_email ON users (email);
CREATE INDEX ix_users_role         ON users (role);
CREATE INDEX ix_users_status       ON users (status);
