-- LA BOUSSOLE — make every Malian tenure document actually storable.
--
-- `land_documents.type` was VARCHAR(20), but two values of the DocumentType
-- enum are longer than that:
--
--   ATTESTATION_COUTUMIERE  → 22 characters
--   LETTRE_ATTRIBUTION      → 18 characters (fitted, barely)
--
-- So an attestation de vente coutumière — one of the three tenure regimes the
-- product exists to reconcile, alongside the Titre Foncier and the Permis
-- d'Occuper — could never be persisted: the insert failed with a data
-- truncation error. The document type most common outside Bamako was the one
-- the schema could not hold.
--
-- Widened to 32, which clears the longest current value with room for the
-- customary and administrative types still to come.

ALTER TABLE land_documents
    MODIFY COLUMN type VARCHAR(32) NOT NULL;
