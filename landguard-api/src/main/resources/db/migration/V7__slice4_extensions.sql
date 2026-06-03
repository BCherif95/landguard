-- LA BOUSSOLE — slice 4 extensions.
-- MySQL version.

CREATE TABLE succession_votes (
    id VARCHAR(36) PRIMARY KEY,
    succession_plan_id VARCHAR(36) NOT NULL,
    heir_id VARCHAR(36) NOT NULL,
    approved BOOLEAN NOT NULL,
    voted_at DATETIME(6) NOT NULL,
    ip_address VARCHAR(45),
    CONSTRAINT fk_vote_plan FOREIGN KEY (succession_plan_id) REFERENCES succession_plans(id),
    CONSTRAINT fk_vote_heir FOREIGN KEY (heir_id) REFERENCES heirs(id)
);

CREATE TABLE succession_audit_events (
    id VARCHAR(36) PRIMARY KEY,
    succession_plan_id VARCHAR(36) NOT NULL,
    type VARCHAR(50) NOT NULL,
    actor VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    metadata JSON,
    CONSTRAINT fk_audit_plan FOREIGN KEY (succession_plan_id) REFERENCES succession_plans(id)
);

CREATE TABLE blockchain_records (
    id VARCHAR(36) PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    hash TEXT NOT NULL,
    previous_hash TEXT,
    anchored_at DATETIME(6) NOT NULL,
    network VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL
);

CREATE TABLE legal_disputes (
    id VARCHAR(36) PRIMARY KEY,
    parcel_id VARCHAR(36) NOT NULL,
    reason TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(50) NOT NULL,
    opened_at DATETIME(6) NOT NULL,
    resolved_at DATETIME(6),
    CONSTRAINT fk_dispute_parcel FOREIGN KEY (parcel_id) REFERENCES land_parcels(id)
);

CREATE INDEX idx_vote_plan_id ON succession_votes(succession_plan_id);
CREATE INDEX idx_audit_plan_id ON succession_audit_events(succession_plan_id);
CREATE INDEX idx_blockchain_entity ON blockchain_records(entity_type, entity_id);
CREATE INDEX idx_dispute_parcel_id ON legal_disputes(parcel_id);
