package com.laboussole.interfaces.rest.certification;

import com.laboussole.application.service.TitleVerificationWorkflowService;
import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.certification.CertificationId;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.out.TitleVerificationRepository;
import com.laboussole.interfaces.rest.certification.dto.LandTitleDto;
import com.laboussole.interfaces.rest.certification.dto.TitleVerificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/title-verifications")
@Tag(name = "Title Verification", description = "Realistic Malian Land Title Verification Workflow")
@RequiredArgsConstructor
public class TitleVerificationController {

    private final TitleVerificationWorkflowService workflowService;
    private final TitleVerificationRepository repository;

    public record OpenCaseRequest(UUID parcelId, String caseReference, LandTitleDto reportedTitle) {}

    @PostMapping
    @Operation(summary = "Open a new title verification case for a Malian Land Title")
    public ResponseEntity<TitleVerificationResponse> openCase(
            @RequestBody OpenCaseRequest request,
            @AuthenticationPrincipal String userId) {
        var caseObj = workflowService.openCase(
                new ParcelId(request.parcelId()),
                request.caseReference(),
                request.reportedTitle().toDomain(),
                new UserId(UUID.fromString(userId))
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
    @Operation(summary = "Mark case as under control at the Domain office")
    public ResponseEntity<Void> startDomainControl(@PathVariable UUID id) {
        workflowService.startDomainControl(new CertificationId(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/requisition")
    @Operation(summary = "Record the result of the manual Domain requisition")
    public ResponseEntity<Void> recordRequisition(
            @PathVariable UUID id,
            @RequestBody TitleVerificationResponse.RequisitionDto requisitionDto) {
        workflowService.recordRequisition(new CertificationId(id), requisitionDto.toDomain());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/certify")
    @Operation(summary = "Final certification by a Notary or authorized Agent")
    public ResponseEntity<Void> certify(
            @PathVariable UUID id,
            @AuthenticationPrincipal String authorityId) {
        workflowService.certify(new CertificationId(id), new UserId(UUID.fromString(authorityId)));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TitleVerificationResponse> getCase(@PathVariable UUID id) {
        return repository.findById(new CertificationId(id))
                .map(TitleVerificationResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
