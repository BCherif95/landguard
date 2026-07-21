package com.laboussole.application.usecase.blockchain;

import com.laboussole.domain.model.blockchain.BlockchainRecord;
import com.laboussole.domain.model.blockchain.LedgerBreakType;
import com.laboussole.domain.model.blockchain.LedgerIntegrityStatus;
import com.laboussole.domain.model.blockchain.LedgerSeal;
import com.laboussole.domain.port.out.blockchain.BlockchainRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifyLedgerIntegrityServiceTest {

    private static final String NETWORK = "LANDGUARD_LOCAL_LEDGER";

    @Mock private BlockchainRecordRepository records;

    private VerifyLedgerIntegrityService service;

    @BeforeEach
    void setUp() {
        service = new VerifyLedgerIntegrityService(records);
    }

    @Test
    @DisplayName("an empty ledger is reported as EMPTY, not as intact")
    void emptyLedger() {
        givenChain(List.of());

        var report = service.execute();

        assertThat(report.status()).isEqualTo(LedgerIntegrityStatus.EMPTY);
        assertThat(report.recordsChecked()).isZero();
    }

    @Test
    @DisplayName("a well-formed chain is INTACT")
    void intactChain() {
        givenChain(chainOf("alpha", "beta", "gamma"));

        var report = service.execute();

        assertThat(report.status()).isEqualTo(LedgerIntegrityStatus.INTACT);
        assertThat(report.recordsChecked()).isEqualTo(3);
        assertThat(report.sealedRecords()).isEqualTo(3);
        assertThat(report.breaks()).isEmpty();
    }

    @Test
    @DisplayName("editing a record's payload after anchoring is caught as SEAL_MISMATCH")
    void tamperedPayloadIsDetected() {
        var chain = new ArrayList<>(chainOf("alpha", "beta", "gamma"));
        var target = chain.get(1);
        // An attacker rewrites the sealed content but cannot recompute the hash
        // of every subsequent record, so the stored hash no longer matches.
        chain.set(1, withPayloadHash(target, LedgerSeal.payloadDigest("beta-falsified")));
        givenChain(chain);

        var report = service.execute();

        assertThat(report.status()).isEqualTo(LedgerIntegrityStatus.BROKEN);
        assertThat(report.breaks())
                .extracting(brk -> brk.type())
                .containsExactly(LedgerBreakType.SEAL_MISMATCH);
        assertThat(report.breaks().getFirst().detail())
                .isEqualTo("Sceau invalide : l'entrée n° 1 a été modifiée après son ancrage.");
    }

    @Test
    @DisplayName("removing a record from the middle breaks both the index and the link")
    void deletedRecordIsDetected() {
        var chain = new ArrayList<>(chainOf("alpha", "beta", "gamma"));
        chain.remove(1);
        givenChain(chain);

        var report = service.execute();

        assertThat(report.status()).isEqualTo(LedgerIntegrityStatus.BROKEN);
        assertThat(report.breaks())
                .extracting(brk -> brk.type())
                .containsExactly(LedgerBreakType.INDEX_GAP, LedgerBreakType.BROKEN_LINK);
    }

    @Test
    @DisplayName("re-parenting a record onto a foreign hash is caught as BROKEN_LINK")
    void reparentedRecordIsDetected() {
        var chain = new ArrayList<>(chainOf("alpha", "beta"));
        var forged = LedgerSeal.payloadDigest("forged-parent");
        var target = chain.get(1);
        chain.set(1, new BlockchainRecord(
                target.id(), target.chainIndex(), target.entityType(), target.entityId(),
                LedgerSeal.chainHash("0x" + forged, target.payloadHash()), "0x" + forged,
                target.payloadHash(), target.anchoredAt(), NETWORK, target.transactionId()));
        givenChain(chain);

        var report = service.execute();

        assertThat(report.breaks())
                .extracting(brk -> brk.type())
                .containsExactly(LedgerBreakType.BROKEN_LINK);
    }

    @Test
    @DisplayName("pre-migration rows are reported as unsealed, never as verified")
    void legacyRowsAreNotClaimedVerified() {
        var chain = new ArrayList<>(chainOf("alpha", "beta"));
        chain.set(0, withPayloadHash(chain.get(0), null));
        givenChain(chain);

        var report = service.execute();

        assertThat(report.status()).isEqualTo(LedgerIntegrityStatus.PARTIALLY_VERIFIABLE);
        assertThat(report.unsealedRecords()).isEqualTo(1);
        assertThat(report.sealedRecords()).isEqualTo(1);
        assertThat(report.breaks()).isEmpty();
    }

    @Test
    @DisplayName("the replay walks every page, not just the first")
    void chainLongerThanOnePageIsFullyWalked() {
        var payloads = new String[VerifyLedgerIntegrityService.PAGE_SIZE + 25];
        for (int i = 0; i < payloads.length; i++) {
            payloads[i] = "payload-" + i;
        }
        givenChain(chainOf(payloads));

        var report = service.execute();

        assertThat(report.status()).isEqualTo(LedgerIntegrityStatus.INTACT);
        assertThat(report.recordsChecked()).isEqualTo(payloads.length);
    }

    /** Serves the chain through the paged port the service actually uses. */
    private void givenChain(List<BlockchainRecord> chain) {
        var ordered = chain.stream()
                .sorted(Comparator.comparingLong(BlockchainRecord::chainIndex))
                .toList();
        when(records.findChainSlice(anyLong(), anyInt())).thenAnswer(invocation -> {
            long from = invocation.getArgument(0);
            int limit = invocation.getArgument(1);
            return ordered.stream()
                    .filter(record -> record.chainIndex() >= from)
                    .limit(limit)
                    .toList();
        });
    }

    /** Builds a genuinely chained ledger from successive payloads. */
    private static List<BlockchainRecord> chainOf(String... payloads) {
        var chain = new ArrayList<BlockchainRecord>(payloads.length);
        String previousHash = LedgerSeal.GENESIS_PREVIOUS_HASH;
        for (int index = 0; index < payloads.length; index++) {
            var record = BlockchainRecord.seal(
                    index, "SUCCESSION_PLAN", "plan-" + index, previousHash, payloads[index], NETWORK);
            chain.add(record);
            previousHash = record.hash();
        }
        return chain;
    }

    private static BlockchainRecord withPayloadHash(BlockchainRecord record, String payloadHash) {
        return new BlockchainRecord(
                record.id(), record.chainIndex(), record.entityType(), record.entityId(),
                record.hash(), record.previousHash(), payloadHash,
                record.anchoredAt(), record.network(), record.transactionId());
    }
}
