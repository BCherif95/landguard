package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.blockchain.BlockchainRecord;
import com.laboussole.domain.port.out.blockchain.BlockchainRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BlockchainRecordRepositoryAdapter implements BlockchainRecordRepository {

    private final BlockchainRecordSpringDataRepository repository;

    @Override
    public void save(BlockchainRecord record) {
        BlockchainRecordJpaEntity entity = new BlockchainRecordJpaEntity(
                record.id(),
                record.chainIndex(),
                record.entityType(),
                record.entityId(),
                record.hash(),
                record.previousHash(),
                record.payloadHash(),
                record.anchoredAt(),
                record.network(),
                record.transactionId()
        );
        repository.save(entity);
    }

    @Override
    public List<BlockchainRecord> findByEntityId(String entityId) {
        return repository.findByEntityId(entityId).stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public Optional<BlockchainRecord> findByHash(String hash) {
        return repository.findByHash(hash).map(this::mapToDomain);
    }

    @Override
    public List<BlockchainRecord> findMostRecent(int limit) {
        // Ordered by chain index, not by anchoredAt: two anchors sharing a
        // microsecond would otherwise come back in an arbitrary order.
        return repository
                .findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "chainIndex")))
                .getContent().stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public Optional<BlockchainRecord> lockChainHead() {
        return repository.lockChainHead().map(this::mapToDomain);
    }

    @Override
    public List<BlockchainRecord> findChainSlice(long fromChainIndex, int limit) {
        return repository
                .findByChainIndexGreaterThanEqualOrderByChainIndexAsc(
                        fromChainIndex, PageRequest.ofSize(limit))
                .stream()
                .map(this::mapToDomain)
                .toList();
    }

    private BlockchainRecord mapToDomain(BlockchainRecordJpaEntity entity) {
        return new BlockchainRecord(
                entity.getId(),
                entity.getChainIndex(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getHash(),
                entity.getPreviousHash(),
                entity.getPayloadHash(),
                entity.getAnchoredAt(),
                entity.getNetwork(),
                entity.getTransactionId()
        );
    }
}
