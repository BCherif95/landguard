-- LA BOUSSOLE — monitoring domain.
-- MySQL version.

CREATE TABLE monitoring_events (
    id VARCHAR(36) PRIMARY KEY,
    parcel_id VARCHAR(36) NOT NULL,
    type VARCHAR(50) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    detected_at DATETIME(6) NOT NULL,
    confidence_score INTEGER NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    description TEXT NOT NULL,
    source VARCHAR(50) NOT NULL,
    image_url TEXT,
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_monitoring_parcel FOREIGN KEY (parcel_id) REFERENCES land_parcels(id)
);

CREATE TABLE satellite_snapshots (
    id VARCHAR(36) PRIMARY KEY,
    parcel_id VARCHAR(36) NOT NULL,
    captured_at DATETIME(6) NOT NULL,
    image_url TEXT NOT NULL,
    movement_score INTEGER NOT NULL,
    anomaly_score INTEGER NOT NULL,
    metadata TEXT,
    CONSTRAINT fk_snapshot_parcel FOREIGN KEY (parcel_id) REFERENCES land_parcels(id)
);

CREATE INDEX idx_monitoring_parcel_id ON monitoring_events(parcel_id);
CREATE INDEX idx_monitoring_severity ON monitoring_events(severity);
CREATE INDEX idx_snapshot_parcel_id ON satellite_snapshots(parcel_id);
