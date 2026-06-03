package com.laboussole.interfaces.rest.legal;

import com.laboussole.application.usecase.legal.LegalDossier;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.in.legal.GenerateLegalProofUseCase;
import com.laboussole.domain.port.in.legal.OpenLegalDisputeUseCase;
import com.laboussole.domain.port.in.legal.ResolveLegalDisputeUseCase;
import com.laboussole.domain.port.out.legal.LegalDisputeRepository;
import com.laboussole.interfaces.rest.legal.dto.DisputeResponse;
import com.laboussole.interfaces.rest.legal.dto.OpenDisputeRequest;
import com.laboussole.application.service.PdfGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/legal")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Legal", description = "Legal disputes and proof generation.")
@RequiredArgsConstructor
public class LegalController {

    private final OpenLegalDisputeUseCase openDisputeUseCase;
    private final ResolveLegalDisputeUseCase resolveDisputeUseCase;
    private final GenerateLegalProofUseCase generateProofUseCase;
    private final LegalDisputeRepository disputeRepository;
    private final PdfGenerationService pdfGenerationService;

    @Operation(summary = "List all legal disputes.")
    @GetMapping("/disputes")
    public List<DisputeResponse> list() {
        return disputeRepository.findAll().stream()
                .map(DisputeResponse::from)
                .toList();
    }

    @Operation(summary = "Open a new legal dispute for a parcel.")
    @PostMapping("/disputes")
    public UUID open(@Valid @RequestBody OpenDisputeRequest request) {
        return openDisputeUseCase.open(ParcelId.of(request.parcelId()), request.reason(), request.severity());
    }

    @Operation(summary = "Resolve a legal dispute.")
    @PostMapping("/disputes/{id}/resolve")
    public void resolve(@PathVariable UUID id) {
        resolveDisputeUseCase.resolve(id);
    }

    @Operation(summary = "Generate a legal proof dossier for a parcel.")
    @PostMapping("/generate-proof")
    public LegalDossier generateProof(@RequestParam UUID parcelId) {
        return generateProofUseCase.generate(ParcelId.of(parcelId));
    }

    @Operation(summary = "Export a legal dossier as PDF.")
    @GetMapping("/export-dossier")
    public ResponseEntity<byte[]> exportDossier(@RequestParam UUID parcelId) {
        LegalDossier dossier = generateProofUseCase.generate(ParcelId.of(parcelId));
        byte[] pdf = pdfGenerationService.generateLegalDossier(dossier);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dossier_" + parcelId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
