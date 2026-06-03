package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.blockchain.BlockchainRecord;
import com.laboussole.domain.port.out.blockchain.BlockchainRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BlockchainRecordRepositoryAdapter implements BlockchainRecordRepository {

    private final BlockchainRecordSpringDataRepository repository;

    @Override
    public void save(BlockchainRecord record) {
        BlockchainRecordJpaEntity entity = new BlockchainRecordJpaEntity(
                record.id(),
                record.entityType(),
                record.entityId(),
                record.hash(),
                record.previousHash(),
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
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BlockchainRecord> findByHash(String hash) {
        return repository.findByHash(hash).map(this::mapToDomain);
    }

    private BlockchainRecord mapToDomain(BlockchainRecordJpaEntity entity) {
        return new BlockchainRecord(
                entity.getId(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getHash(),
                entity.getPreviousHash(),
                entity.getAnchoredAt(),
                entity.getNetwork(),
                entity.getTransactionId()
        );
    }
}
