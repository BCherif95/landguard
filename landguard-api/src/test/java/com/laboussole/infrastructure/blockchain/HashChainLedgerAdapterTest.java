package com.laboussole.infrastructure.blockchain;

import com.laboussole.domain.model.blockchain.BlockchainRecord;
import com.laboussole.domain.model.blockchain.LedgerSeal;
import com.laboussole.domain.port.out.blockchain.BlockchainRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class HashChainLedgerAdapterTest {

    private InMemoryLedgerRepository repository;
    private HashChainLedgerAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = new InMemoryLedgerRepository();
        adapter = new HashChainLedgerAdapter(repository);
    }

    @Test
    @DisplayName("the first anchor starts the chain at index 0 from the genesis sentinel")
    void firstAnchorIsGenesis() {
        var record = adapter.anchor("SUCCESSION_PLAN", "plan-1", "status=DRAFT");

        assertThat(record.chainIndex()).isZero();
        assertThat(record.previousHash()).isEqualTo(LedgerSeal.GENESIS_PREVIOUS_HASH);
        assertThat(record.hash()).hasSize(LedgerSeal.HASH_LENGTH).startsWith("0x");
    }

    @Test
    @DisplayName("each anchor chains onto the previous hash and increments the index")
    void anchorsAreChained() {
        var first = adapter.anchor("SUCCESSION_PLAN", "plan-1", "status=DRAFT");
        var second = adapter.anchor("SUCCESSION_PLAN", "plan-1", "status=VALIDATED");
        var third = adapter.anchor("CERTIFICATION", "cert-9", "status=VERIFIED");

        assertThat(second.previousHash()).isEqualTo(first.hash());
        assertThat(third.previousHash()).isEqualTo(second.hash());
        assertThat(List.of(first.chainIndex(), second.chainIndex(), third.chainIndex()))
                .containsExactly(0L, 1L, 2L);
    }

    @Test
    @DisplayName("the stored seal is the SHA-256 of the parent hash and the payload digest")
    void sealIsRecomputable() {
        var record = adapter.anchor("SUCCESSION_PLAN", "plan-1", "status=DRAFT");

        assertThat(record.payloadHash()).isEqualTo(LedgerSeal.payloadDigest("status=DRAFT"));
        assertThat(record.hash())
                .isEqualTo(LedgerSeal.chainHash(record.previousHash(), record.payloadHash()));
    }

    @Test
    @DisplayName("an identical payload anchored twice yields different seals — position matters")
    void identicalPayloadsDoNotCollide() {
        var first = adapter.anchor("SUCCESSION_PLAN", "plan-1", "status=DRAFT");
        var second = adapter.anchor("SUCCESSION_PLAN", "plan-1", "status=DRAFT");

        assertThat(second.payloadHash()).isEqualTo(first.payloadHash());
        assertThat(second.hash()).isNotEqualTo(first.hash());
    }

    @Test
    @DisplayName("anchors always take the highest index, never the most recently saved row")
    void headIsResolvedByIndexNotByInsertionOrder() {
        adapter.anchor("SUCCESSION_PLAN", "plan-1", "first");
        var second = adapter.anchor("SUCCESSION_PLAN", "plan-2", "second");

        // Simulates rows coming back in a different physical order than written.
        repository.shuffleStorage();
        var third = adapter.anchor("SUCCESSION_PLAN", "plan-3", "third");

        assertThat(third.chainIndex()).isEqualTo(2L);
        assertThat(third.previousHash()).isEqualTo(second.hash());
    }

    /** Minimal in-memory ledger; the lock is a no-op since the test is single-threaded. */
    private static final class InMemoryLedgerRepository implements BlockchainRecordRepository {

        private final List<BlockchainRecord> storage = new ArrayList<>();

        @Override
        public void save(BlockchainRecord record) {
            storage.add(record);
        }

        @Override
        public List<BlockchainRecord> findByEntityId(String entityId) {
            return storage.stream().filter(r -> r.entityId().equals(entityId)).toList();
        }

        @Override
        public Optional<BlockchainRecord> findByHash(String hash) {
            return storage.stream().filter(r -> r.hash().equals(hash)).findFirst();
        }

        @Override
        public List<BlockchainRecord> findMostRecent(int limit) {
            return storage.stream()
                    .sorted(Comparator.comparingLong(BlockchainRecord::chainIndex).reversed())
                    .limit(limit)
                    .toList();
        }

        @Override
        public Optional<BlockchainRecord> lockChainHead() {
            return storage.stream().max(Comparator.comparingLong(BlockchainRecord::chainIndex));
        }

        @Override
        public List<BlockchainRecord> findChainSlice(long fromChainIndex, int limit) {
            return storage.stream()
                    .filter(r -> r.chainIndex() >= fromChainIndex)
                    .sorted(Comparator.comparingLong(BlockchainRecord::chainIndex))
                    .limit(limit)
                    .toList();
        }

        void shuffleStorage() {
            storage.sort(Comparator.comparingLong(BlockchainRecord::chainIndex).reversed());
        }
    }
}
