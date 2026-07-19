package com.laboussole.interfaces.rest.certification;

import com.laboussole.application.service.PdfGenerationService;
import com.laboussole.application.service.TitleVerificationWorkflowService;
import com.laboussole.domain.model.certification.CertificationId;
import com.laboussole.domain.model.certification.VerificationStatus;
import com.laboussole.infrastructure.notification.AlertNotificationProperties;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.out.TitleVerificationRepository;
import com.laboussole.infrastructure.security.AuthenticatedPrincipal;
import com.laboussole.interfaces.rest.certification.dto.LandTitleDto;
import com.laboussole.interfaces.rest.certification.dto.TitleVerificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/title-verifications")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Title Verification", description = "Malian Land Title verification workflow (case lifecycle).")
@RequiredArgsConstructor
public class TitleVerificationController {

    private final TitleVerificationWorkflowService workflowService;
    private final TitleVerificationRepository repository;
    private final PdfGenerationService pdfService;
    private final AlertNotificationProperties notificationProperties;

    public record OpenCaseRequest(UUID parcelId, String caseReference, LandTitleDto reportedTitle) {}

    @PostMapping
    @Operation(summary = "Open a new title verification case for a Malian Land Title")
    public ResponseEntity<TitleVerificationResponse> openCase(
            @RequestBody OpenCaseRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        var caseObj = workflowService.openCase(
                new ParcelId(request.parcelId()),
                request.caseReference(),
                request.reportedTitle().toDomain(),
                principal.userId()
        );
        return ResponseEntity.ok(TitleVerificationResponse.fromDomain(caseObj));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit case for professional verification")
    public ResponseEntity<Void> submitForVerification(@PathVariable UUID id) {
        workflowService.submitForVerification(new CertificationId(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/start-domain-control")
    @Operation(summary = "Mark case as under control at the Domain office (officers, legal, admins only)")
    @PreAuthorize("hasAnyRole('OFFICER', 'LEGAL', 'ADMIN')")
    public ResponseEntity<Void> startDomainControl(@PathVariable UUID id) {
        workflowService.startDomainControl(new CertificationId(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/requisition")
    @Operation(summary = "Record the result of the manual Domain requisition (officers, legal, admins only)")
    @PreAuthorize("hasAnyRole('OFFICER', 'LEGAL', 'ADMIN')")
    public ResponseEntity<Void> recordRequisition(
            @PathVariable UUID id,
            @RequestBody TitleVerificationResponse.RequisitionDto requisitionDto) {
        workflowService.recordRequisition(new CertificationId(id), requisitionDto.toDomain());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/certify")
    @Operation(summary = "Final certification by a Notary or authorized Agent (officers, legal, admins only)")
    @PreAuthorize("hasAnyRole('OFFICER', 'LEGAL', 'ADMIN')")
    public ResponseEntity<Void> certify(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        workflowService.certify(new CertificationId(id), principal.userId());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "List verification cases for a parcel (at most one per parcel). Empty list when none is open.")
    public List<TitleVerificationResponse> listByParcel(@RequestParam("parcelId") UUID parcelId) {
        return repository.findByParcelId(new ParcelId(parcelId))
                .map(TitleVerificationResponse::fromDomain)
                .map(List::of)
                .orElseGet(List::of);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TitleVerificationResponse> getCase(@PathVariable UUID id) {
        return repository.findById(new CertificationId(id))
                .map(TitleVerificationResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/certificate")
    @Operation(summary = "Download the Certificat de Vigilance PDF (CERTIFIED cases only). "
            + "The embedded QR code resolves to the public verification page.")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable UUID id) {
        return repository.findById(new CertificationId(id))
                .map(kase -> {
                    if (kase.status() != VerificationStatus.CERTIFIED) {
                        return ResponseEntity.status(409).<byte[]>build();
                    }
                    var verificationUrl = notificationProperties.frontendBaseUrl()
                            + "/verifier/" + kase.caseReference();
                    byte[] pdf = pdfService.generateVigilanceCertificate(kase, verificationUrl);
                    return ResponseEntity.ok()
                            .header("Content-Type", "application/pdf")
                            .header("Content-Disposition", "attachment; filename=Certificat_Vigilance_"
                                    + kase.caseReference() + ".pdf")
                            .body(pdf);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
