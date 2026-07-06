-- LA BOUSSOLE — OCR extraction results (Épic Cartographie IA, section 4.2).
-- MySQL version.
--
-- One row per uploaded document, written at upload time. Linked to
-- land_documents through storage_key rather than a foreign key: the
-- land_documents row is only created later, when the parcel registration
-- is submitted (upload precedes registration in the wizard).
--
-- OCR results are advisory only — they pre-fill forms and flag anomalies.
-- Final document validation is always human (PRD Feature 02.2).

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
