-- LA BOUSSOLE — per-user alert notification preferences (Epic 3, Feature 03.2).
-- MySQL version.
--
-- Defaults mirror NotificationPreferences.defaults(): push + e-mail on,
-- SMS off until the user records a phone number.

CREATE TABLE notification_preferences (
    user_id       VARCHAR(36) PRIMARY KEY,
    push_enabled  BOOLEAN      NOT NULL DEFAULT TRUE,
    email_enabled BOOLEAN      NOT NULL DEFAULT TRUE,
    sms_enabled   BOOLEAN      NOT NULL DEFAULT FALSE,
    phone_number  VARCHAR(20),
    updated_at    DATETIME(6)  NOT NULL,
    CONSTRAINT fk_notification_preferences_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
