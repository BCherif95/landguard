-- LA BOUSSOLE — heritage domain.
-- MySQL version.

CREATE TABLE succession_plans (
    id VARCHAR(36) PRIMARY KEY,
    parcel_id VARCHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL,
    blockchain_hash TEXT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_succession_parcel FOREIGN KEY (parcel_id) REFERENCES land_parcels(id)
);

CREATE TABLE heirs (
    id VARCHAR(36) PRIMARY KEY,
    plan_id VARCHAR(36) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    relation VARCHAR(100) NOT NULL,
    share_percentage INTEGER NOT NULL,
    validated BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_heir_plan FOREIGN KEY (plan_id) REFERENCES succession_plans(id)
);

CREATE INDEX idx_succession_parcel_id ON succession_plans(parcel_id);
CREATE INDEX idx_heir_plan_id ON heirs(plan_id);
