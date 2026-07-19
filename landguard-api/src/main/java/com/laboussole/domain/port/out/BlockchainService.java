package com.laboussole.domain.port.out;

import com.laboussole.domain.model.blockchain.BlockchainRecord;

/**
 * Driven port: append-only cryptographic ledger. An anchor takes a canonical
 * payload, seals it with SHA-256 chained to the previous ledger entry, persists
 * the resulting record and returns it. Tampering with any historical entry
 * breaks the chain and is therefore detectable.
 */
public interface BlockchainService {

    BlockchainRecord anchor(String entityType, String entityId, String canonicalPayload);
}
