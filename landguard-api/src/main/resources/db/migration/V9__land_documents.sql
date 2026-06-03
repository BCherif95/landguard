-- LA BOUSSOLE — land documents table.

CREATE TABLE land_documents (
    id VARCHAR(36) PRIMARY KEY,
    parcel_id VARCHAR(36) NOT NULL,
    type VARCHAR(20) NOT NULL,
    label VARCHAR(200) NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100),
    uploaded_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_document_parcel FOREIGN KEY (parcel_id) REFERENCES land_parcels(id)
);

CREATE INDEX idx_document_parcel_id ON land_documents(parcel_id);
