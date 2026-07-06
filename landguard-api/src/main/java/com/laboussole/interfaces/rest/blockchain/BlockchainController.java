package com.laboussole.interfaces.rest.blockchain;

import com.laboussole.domain.port.out.blockchain.BlockchainRecordRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blockchain")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Blockchain", description = "Anchored records (succession plans, certifications).")
class BlockchainController {

    private static final int MAX_LIMIT = 200;

    private final BlockchainRecordRepository records;

    BlockchainController(BlockchainRecordRepository records) {
        this.records = records;
    }

    @Operation(summary = "List the most recent blockchain anchors. Returns an empty list when nothing has been anchored yet.")
    @GetMapping("/records")
    public List<BlockchainRecordResponse> list(
            @RequestParam(value = "entityId", required = false) String entityId,
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        int cappedLimit = Math.max(1, Math.min(limit, MAX_LIMIT));
        var found = entityId != null && !entityId.isBlank()
                ? records.findByEntityId(entityId)
                : records.findMostRecent(cappedLimit);
        return found.stream().map(BlockchainRecordResponse::from).toList();
    }
}
