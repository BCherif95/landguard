-- LA BOUSSOLE — ledger integrity hardening.
--
-- The append-only ledger shipped with three structural weaknesses:
--
--   1. no strict total order — records were ordered by anchored_at, which ties
--      whenever two anchors land in the same microsecond, leaving the chain
--      head ambiguous;
--   2. nothing stopped two concurrent anchors from adopting the same
--      previous_hash, which forks the chain into two competing branches;
--   3. the canonical payload was hashed then discarded, so no seal could ever
--      be recomputed — the chain was, by construction, unverifiable.
--
-- This migration adds chain_index (strict order), payload_hash (recomputable
-- seal) and the uniqueness constraints that make a fork impossible.
--
-- Rows anchored before this migration keep payload_hash NULL: their link to the
-- previous record stays checkable, but their seal is not recomputable. The
-- verification endpoint reports them as unsealed rather than as valid.
--
-- Delivered as a separate migration rather than an edit to the V1 baseline so
-- that databases which already applied V1 keep a valid Flyway checksum.

ALTER TABLE blockchain_records
    ADD COLUMN chain_index  BIGINT      NULL AFTER id,
    ADD COLUMN payload_hash VARCHAR(64) NULL AFTER previous_hash;

-- Backfill a deterministic position for pre-existing rows. Window function
-- rather than a session variable: the latter is deprecated for this use.
UPDATE blockchain_records target
    JOIN (
        SELECT id,
               ROW_NUMBER() OVER (ORDER BY anchored_at ASC, id ASC) - 1 AS position
          FROM blockchain_records
    ) ordered ON ordered.id = target.id
   SET target.chain_index = ordered.position;

-- Genesis rows were always written with the sentinel, but the column allowed
-- NULL; normalise before enforcing NOT NULL.
UPDATE blockchain_records
   SET previous_hash = CONCAT('0x', REPEAT('0', 64))
 WHERE previous_hash IS NULL;

ALTER TABLE blockchain_records
    MODIFY COLUMN chain_index   BIGINT      NOT NULL,
    MODIFY COLUMN hash          VARCHAR(66) NOT NULL,
    MODIFY COLUMN previous_hash VARCHAR(66) NOT NULL;

-- A fork is now structurally impossible: no two records may claim the same
-- position, the same seal, or the same parent.
--
-- If one of these statements fails, the ledger is ALREADY forked or duplicated.
-- That is not a migration defect — it is a genuine integrity incident that must
-- be triaged before the service is allowed to start.
CREATE UNIQUE INDEX ux_blockchain_chain_index   ON blockchain_records (chain_index);
CREATE UNIQUE INDEX ux_blockchain_hash          ON blockchain_records (hash);
CREATE UNIQUE INDEX ux_blockchain_previous_hash ON blockchain_records (previous_hash);
