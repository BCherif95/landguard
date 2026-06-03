package com.laboussole.infrastructure.blockchain;

import com.laboussole.domain.model.heritage.SuccessionPlan;
import com.laboussole.domain.port.out.BlockchainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MockBlockchainAdapter implements BlockchainService {

    @Override
    public CompletableFuture<String> anchor(SuccessionPlan plan) {
        log.info("Anchoring succession plan {} on blockchain for parcel {}...", plan.id(), plan.parcelId());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate network latency
                TimeUnit.SECONDS.sleep(2);
                
                String dataToHash = plan.id().toString() + plan.parcelId().toString() + plan.updatedAt().toString();
                String hash = generateSha256(dataToHash);
                
                log.info("Succession plan {} anchored with SHA-256 hash: {}", plan.id(), hash);
                return "0x" + hash;
            } catch (InterruptedException | NoSuchAlgorithmException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                throw new RuntimeException("Blockchain anchoring failed", e);
            }
        });
    }

    private String generateSha256(String originalString) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(originalString.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(encodedhash);
    }
}
