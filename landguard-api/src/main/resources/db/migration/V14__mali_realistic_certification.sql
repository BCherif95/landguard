-- Mali Realistic Land Certification Refactoring
ALTER TABLE parcel_certifications RENAME TO title_verification_cases;

ALTER TABLE title_verification_cases 
    CHANGE COLUMN certification_number case_reference VARCHAR(100),
    DROP COLUMN authenticity_score,
    DROP COLUMN fraud_risk_score,
    DROP COLUMN legal_risk_score;

ALTER TABLE title_verification_cases 
    ADD COLUMN tf_number VARCHAR(100),
    ADD COLUMN tf_volume VARCHAR(50),
    ADD COLUMN tf_folio VARCHAR(50),
    ADD COLUMN tf_conservation_office VARCHAR(200),
    ADD COLUMN tf_issue_date TIMESTAMP,
    ADD COLUMN tf_owner_name VARCHAR(255),
    ADD COLUMN tf_area_hectares DECIMAL(19, 4),
    ADD COLUMN tf_location VARCHAR(255);

CREATE TABLE title_verification_requisitions (
    case_id BINARY(16) PRIMARY KEY,
    requisition_number VARCHAR(100) NOT NULL,
    requisition_date TIMESTAMP NOT NULL,
    domain_office VARCHAR(200) NOT NULL,
    verifier_name VARCHAR(255) NOT NULL,
    verifier_role VARCHAR(100) NOT NULL,
    verification_notes TEXT,
    authenticity_confirmed BOOLEAN DEFAULT FALSE,
    conflict_detected BOOLEAN DEFAULT FALSE,
    litigation_detected BOOLEAN DEFAULT FALSE,
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (case_id) REFERENCES title_verification_cases(id) ON DELETE CASCADE
);
