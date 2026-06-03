CREATE TABLE land_titles (
    tf_number VARCHAR(100) PRIMARY KEY,
    volume VARCHAR(50) NOT NULL,
    folio VARCHAR(50) NOT NULL,
    conservation_office VARCHAR(200) NOT NULL,
    issue_date TIMESTAMP NOT NULL,
    owner_name VARCHAR(255) NOT NULL,
    area_hectares DECIMAL(19, 4) NOT NULL,
    location VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE parcel_certifications (
    id BINARY(16) PRIMARY KEY,
    parcel_id BINARY(16) NOT NULL,
    certification_number VARCHAR(100) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL,
    authenticity_score INT DEFAULT 0,
    fraud_risk_score INT DEFAULT 0,
    legal_risk_score INT DEFAULT 0,
    initiated_by BINARY(16) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    certified_at TIMESTAMP NULL,
    certified_by BINARY(16) NULL,
    INDEX idx_cert_parcel (parcel_id),
    INDEX idx_cert_status (status)
);

CREATE TABLE certification_audits (
    id BINARY(16) PRIMARY KEY,
    certification_id BINARY(16) NOT NULL,
    action VARCHAR(100) NOT NULL,
    actor_id BINARY(16) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    decision VARCHAR(50),
    notes TEXT,
    ip_address VARCHAR(45),
    INDEX idx_audit_cert (certification_id)
);

CREATE TABLE fraud_alerts (
    id BINARY(16) PRIMARY KEY,
    certification_id BINARY(16) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    fraud_type VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    recommended_action TEXT,
    is_blocking BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_fraud_cert (certification_id)
);
