package com.laboussole.interfaces.rest.certification;

import com.laboussole.domain.model.certification.VerificationStatus;
import com.laboussole.domain.port.out.TitleVerificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Public verification behind the certificate's QR code (PRD Feature 02.3):
 * scanning a printed Certificat de Vigilance must reveal the live truth of the
 * LandGuard database, with no authentication required.
 */
@RestController
@RequestMapping("/api/v1/public/certificates")
@Tag(name = "Public Certificate Verification",
        description = "Unauthenticated QR-code verification of vigilance certificates.")
@RequiredArgsConstructor
public class PublicCertificateController {

    private final TitleVerificationRepository repository;

    public record PublicCertificateResponse(
            String caseReference,
            String status,
            boolean certified,
            Instant certifiedAt,
            String tfNumber,
            String ownerName,
            String location,
            BigDecimal areaHectares) {
    }

    @GetMapping("/{caseReference}")
    @Operation(summary = "Verify a vigilance certificate by its case reference (QR code target).")
    public ResponseEntity<PublicCertificateResponse> verify(@PathVariable String caseReference) {
        return repository.findByReference(caseReference)
                .map(kase -> ResponseEntity.ok(new PublicCertificateResponse(
                        kase.caseReference(),
                        kase.status().name(),
                        kase.status() == VerificationStatus.CERTIFIED,
                        kase.certifiedAt(),
                        kase.reportedTitle().tfNumber(),
                        kase.reportedTitle().ownerName(),
                        kase.reportedTitle().location(),
                        kase.reportedTitle().area().value())))
                .orElse(ResponseEntity.notFound().build());
    }
}
