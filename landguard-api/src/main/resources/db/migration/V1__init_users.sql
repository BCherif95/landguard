-- LA BOUSSOLE — consolidated baseline schema (single-migration strategy).
-- MySQL 8 version.
--
-- This file is the only Flyway migration: it creates the complete schema for
-- every bounded context (identity, cadastral registry, monitoring, heritage,
-- title verification, notifications, OCR). It contains NO seed or demo data.
--
-- Authentication is fully stateless (signed JWTs, expiration embedded in the
-- token): there is deliberately no refresh-token table.
--
-- Applying this baseline on a database that ran the previous V1..V17 chain
-- requires recreating the schema (e.g. `docker compose down -v`).

-- ---------------------------------------------------------------------------
-- Identity
-- ---------------------------------------------------------------------------

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

CREATE UNIQUE INDEX ux_users_email  ON users (email);
CREATE INDEX ix_users_role          ON users (role);
CREATE INDEX ix_users_status        ON users (status);

-- ---------------------------------------------------------------------------
-- Cadastral registry
-- ---------------------------------------------------------------------------

CREATE TABLE land_parcels (
    id                    VARCHAR(36)  PRIMARY KEY,
    reference             VARCHAR(64)  NOT NULL,
    title_number          VARCHAR(32)  UNIQUE,
    name                  VARCHAR(200) NOT NULL,
    region_label          VARCHAR(200) NOT NULL,
    owner_label           VARCHAR(200) NOT NULL,
    owner_user_id         VARCHAR(36),
    area_ha               DECIMAL(14, 4) NOT NULL,
    estimated_value_xof   DECIMAL(18, 0) NOT NULL,
    geometry              GEOMETRY     NOT NULL SRID 4326,
    status                VARCHAR(32)  NOT NULL,
    risk_score            INTEGER      NOT NULL,
    trust_score           INTEGER      NOT NULL,
    created_at            DATETIME(6)  NOT NULL,
    updated_at            DATETIME(6)  NOT NULL,
    last_verified_at      DATETIME(6),
    CONSTRAINT fk_parcels_owner FOREIGN KEY (owner_user_id) REFERENCES users (id) ON DELETE SET NULL,
    CHECK (area_ha >= 0),
    CHECK (estimated_value_xof >= 0),
    CHECK (risk_score BETWEEN 0 AND 100),
    CHECK (trust_score BETWEEN 0 AND 100)
);

CREATE UNIQUE INDEX ux_parcels_reference    ON land_parcels (reference);
CREATE INDEX         ix_parcels_owner       ON land_parcels (owner_user_id);
CREATE INDEX         ix_parcels_status      ON land_parcels (status);
CREATE INDEX         ix_parcels_created     ON land_parcels (created_at DESC);
CREATE SPATIAL INDEX ix_parcels_geometry    ON land_parcels (geometry);

-- Documents are sealed at upload time: sha256_hash is the hex-encoded SHA-256
-- fingerprint of the original file (PRD 2.1 — any later modification of the
-- file breaks the validation chain).
CREATE TABLE land_documents (
    id           VARCHAR(36)  PRIMARY KEY,
    parcel_id    VARCHAR(36)  NOT NULL,
    type         VARCHAR(20)  NOT NULL,
    label        VARCHAR(200) NOT NULL,
    storage_key  VARCHAR(255) NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    content_type VARCHAR(100),
    sha256_hash  VARCHAR(64),
    uploaded_at  DATETIME(6)  NOT NULL,
    CONSTRAINT fk_document_parcel FOREIGN KEY (parcel_id) REFERENCES land_parcels (id)
);

CREATE INDEX idx_document_parcel_id ON land_documents (parcel_id);

-- ---------------------------------------------------------------------------
-- Active monitoring (Epic 3)
-- ---------------------------------------------------------------------------

CREATE TABLE monitoring_events (
    id               VARCHAR(36) PRIMARY KEY,
    parcel_id        VARCHAR(36) NOT NULL,
    type             VARCHAR(50) NOT NULL,
    severity         VARCHAR(50) NOT NULL,
    detected_at      DATETIME(6) NOT NULL,
    confidence_score INTEGER     NOT NULL,
    longitude        DOUBLE PRECISION NOT NULL,
    latitude         DOUBLE PRECISION NOT NULL,
    description      TEXT        NOT NULL,
    source           VARCHAR(50) NOT NULL,
    image_url        TEXT,
    resolved         BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_monitoring_parcel FOREIGN KEY (parcel_id) REFERENCES land_parcels (id)
);

CREATE INDEX idx_monitoring_parcel_id ON monitoring_events (parcel_id);
CREATE INDEX idx_monitoring_severity  ON monitoring_events (severity);

CREATE TABLE satellite_snapshots (
    id             VARCHAR(36) PRIMARY KEY,
    parcel_id      VARCHAR(36) NOT NULL,
    captured_at    DATETIME(6) NOT NULL,
    image_url      TEXT        NOT NULL,
    movement_score INTEGER     NOT NULL,
    anomaly_score  INTEGER     NOT NULL,
    metadata       TEXT,
    CONSTRAINT fk_snapshot_parcel FOREIGN KEY (parcel_id) REFERENCES land_parcels (id)
);

CREATE INDEX idx_snapshot_parcel_id ON satellite_snapshots (parcel_id);

-- ---------------------------------------------------------------------------
-- Heritage / succession (Epic 4)
-- ---------------------------------------------------------------------------

CREATE TABLE succession_plans (
    id              VARCHAR(36) PRIMARY KEY,
    parcel_id       VARCHAR(36) NOT NULL,
    status          VARCHAR(50) NOT NULL,
    blockchain_hash TEXT,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NOT NULL,
    CONSTRAINT fk_succession_parcel FOREIGN KEY (parcel_id) REFERENCES land_parcels (id)
);

CREATE INDEX idx_succession_parcel_id ON succession_plans (parcel_id);

-- user_id links an heir to their platform account (Feature 04.1: several user
-- accounts associated with the same land title). Nullable: heirs without an
-- account can still be recorded.
CREATE TABLE heirs (
    id               VARCHAR(36)  PRIMARY KEY,
    plan_id          VARCHAR(36)  NOT NULL,
    full_name        VARCHAR(255) NOT NULL,
    relation         VARCHAR(100) NOT NULL,
    share_percentage INTEGER      NOT NULL,
    validated        BOOLEAN      NOT NULL DEFAULT FALSE,
    user_id          VARCHAR(36),
    CONSTRAINT fk_heir_plan FOREIGN KEY (plan_id) REFERENCES succession_plans (id),
    CONSTRAINT fk_heir_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_heir_plan_id ON heirs (plan_id);
CREATE INDEX idx_heir_user_id ON heirs (user_id);

CREATE TABLE succession_votes (
    id                 VARCHAR(36) PRIMARY KEY,
    succession_plan_id VARCHAR(36) NOT NULL,
    heir_id            VARCHAR(36) NOT NULL,
    approved           BOOLEAN     NOT NULL,
    voted_at           DATETIME(6) NOT NULL,
    ip_address         VARCHAR(45),
    CONSTRAINT fk_vote_plan FOREIGN KEY (succession_plan_id) REFERENCES succession_plans (id),
    CONSTRAINT fk_vote_heir FOREIGN KEY (heir_id) REFERENCES heirs (id)
);

CREATE INDEX idx_vote_plan_id ON succession_votes (succession_plan_id);

CREATE TABLE succession_audit_events (
    id                 VARCHAR(36)  PRIMARY KEY,
    succession_plan_id VARCHAR(36)  NOT NULL,
    type               VARCHAR(50)  NOT NULL,
    actor              VARCHAR(255) NOT NULL,
    created_at         DATETIME(6)  NOT NULL,
    metadata           JSON,
    CONSTRAINT fk_audit_plan FOREIGN KEY (succession_plan_id) REFERENCES succession_plans (id)
);

CREATE INDEX idx_audit_plan_id ON succession_audit_events (succession_plan_id);

-- Local append-only hash chain: each record's hash is chained to the previous
-- one (previous_hash), so tampering with any historical entry is detectable.
CREATE TABLE blockchain_records (
    id             VARCHAR(36)  PRIMARY KEY,
    entity_type    VARCHAR(50)  NOT NULL,
    entity_id      VARCHAR(255) NOT NULL,
    hash           TEXT         NOT NULL,
    previous_hash  TEXT,
    anchored_at    DATETIME(6)  NOT NULL,
    network        VARCHAR(50)  NOT NULL,
    transaction_id VARCHAR(255) NOT NULL
);

CREATE INDEX idx_blockchain_entity ON blockchain_records (entity_type, entity_id);

-- ---------------------------------------------------------------------------
-- Legal disputes and evidence (Epic 3, Feature 03.3)
-- ---------------------------------------------------------------------------

CREATE TABLE legal_disputes (
    id          VARCHAR(36) PRIMARY KEY,
    parcel_id   VARCHAR(36) NOT NULL,
    reason      TEXT        NOT NULL,
    severity    VARCHAR(20) NOT NULL,
    status      VARCHAR(50) NOT NULL,
    opened_at   DATETIME(6) NOT NULL,
    resolved_at DATETIME(6),
    CONSTRAINT fk_dispute_parcel FOREIGN KEY (parcel_id) REFERENCES land_parcels (id)
);

CREATE INDEX idx_dispute_parcel_id ON legal_disputes (parcel_id);

-- ---------------------------------------------------------------------------
-- Title verification workflow (Epic 2 — "Double Clé")
-- BINARY(16) ids: these entities map UUIDs with Hibernate's default MySQL type.
-- ---------------------------------------------------------------------------

CREATE TABLE title_verification_cases (
    id                     BINARY(16)   PRIMARY KEY,
    parcel_id              BINARY(16)   NOT NULL,
    case_reference         VARCHAR(100) UNIQUE NOT NULL,
    status                 VARCHAR(50)  NOT NULL,
    tf_number              VARCHAR(100),
    tf_volume              VARCHAR(50),
    tf_folio               VARCHAR(50),
    tf_conservation_office VARCHAR(200),
    tf_issue_date          TIMESTAMP,
    tf_owner_name          VARCHAR(255),
    tf_area_hectares       DECIMAL(19, 4),
    tf_location            VARCHAR(255),
    initiated_by           BINARY(16)   NOT NULL,
    created_at             TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    certified_at           TIMESTAMP    NULL,
    certified_by           BINARY(16)   NULL,
    INDEX idx_cert_parcel (parcel_id),
    INDEX idx_cert_status (status)
);

CREATE TABLE title_verification_requisitions (
    case_id                 BINARY(16)   PRIMARY KEY,
    requisition_number      VARCHAR(100) NOT NULL,
    requisition_date        TIMESTAMP    NOT NULL,
    domain_office           VARCHAR(200) NOT NULL,
    verifier_name           VARCHAR(255) NOT NULL,
    verifier_role           VARCHAR(100) NOT NULL,
    verification_notes      TEXT,
    authenticity_confirmed  BOOLEAN      DEFAULT FALSE,
    conflict_detected       BOOLEAN      DEFAULT FALSE,
    litigation_detected     BOOLEAN      DEFAULT FALSE,
    mortgage_detected       BOOLEAN      DEFAULT FALSE,
    name_match_confirmed    BOOLEAN      DEFAULT FALSE,
    rejection_reason        TEXT,
    created_at              TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (case_id) REFERENCES title_verification_cases (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------------
-- Alert notification preferences (Epic 3, Feature 03.2)
-- ---------------------------------------------------------------------------

CREATE TABLE notification_preferences (
    user_id       VARCHAR(36) PRIMARY KEY,
    push_enabled  BOOLEAN     NOT NULL DEFAULT TRUE,
    email_enabled BOOLEAN     NOT NULL DEFAULT TRUE,
    sms_enabled   BOOLEAN     NOT NULL DEFAULT FALSE,
    phone_number  VARCHAR(20),
    updated_at    DATETIME(6) NOT NULL,
    CONSTRAINT fk_notification_preferences_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------------
-- OCR extraction results (PRD 4.2 — advisory only, human validation is final)
-- ---------------------------------------------------------------------------

CREATE TABLE ocr_extraction_results (
    id                    VARCHAR(36)  PRIMARY KEY,
    storage_key           VARCHAR(255) NOT NULL,
    document_type         VARCHAR(20)  NOT NULL,
    status                VARCHAR(32)  NOT NULL,
    title_number          VARCHAR(64),
    owner_name            VARCHAR(200),
    surface_area_hectares DECIMAL(12,4),
    structural_anomalies  TEXT,
    extracted_at          DATETIME(6)  NOT NULL
);

CREATE UNIQUE INDEX ux_ocr_extraction_storage_key ON ocr_extraction_results (storage_key);
