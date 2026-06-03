-- LA BOUSSOLE — land parcel registry table.
-- MySQL version.

CREATE TABLE land_parcels (
    id                    VARCHAR(36)  PRIMARY KEY,
    reference             VARCHAR(64)  NOT NULL,
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

CREATE UNIQUE INDEX ux_parcels_reference   ON land_parcels (reference);
CREATE INDEX        ix_parcels_owner       ON land_parcels (owner_user_id);
CREATE INDEX        ix_parcels_status      ON land_parcels (status);
CREATE INDEX        ix_parcels_created     ON land_parcels (created_at DESC);
CREATE SPATIAL INDEX ix_parcels_geometry    ON land_parcels (geometry);
