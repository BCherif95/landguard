package com.laboussole.interfaces.rest.parcel;

import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.in.GetLandParcelUseCase;
import com.laboussole.domain.port.in.ListLandParcelsUseCase;
import com.laboussole.domain.port.in.RegisterLandParcelUseCase;
import com.laboussole.domain.port.in.VerifyLandParcelUseCase;
import com.laboussole.infrastructure.security.AuthenticatedPrincipal;
import com.laboussole.interfaces.rest.parcel.dto.ParcelResponse;
import com.laboussole.interfaces.rest.parcel.dto.RegisterParcelRequest;
import com.laboussole.interfaces.rest.parcel.dto.VerifyParcelRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/parcels")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Parcels", description = "Land parcel registry — read for any authenticated user, mutations restricted by role.")
class ParcelController {

    private final RegisterLandParcelUseCase registerUseCase;
    private final ListLandParcelsUseCase listUseCase;
    private final GetLandParcelUseCase getUseCase;
    private final VerifyLandParcelUseCase verifyUseCase;
    private final com.laboussole.domain.port.in.TransitionParcelStatusUseCase transitionUseCase;
    private final com.laboussole.domain.port.in.IssueLandTitleUseCase issueTitleUseCase;
    private final com.laboussole.application.service.PdfGenerationService pdfService;

    ParcelController(
            RegisterLandParcelUseCase registerUseCase,
            ListLandParcelsUseCase listUseCase,
            GetLandParcelUseCase getUseCase,
            VerifyLandParcelUseCase verifyUseCase,
            com.laboussole.domain.port.in.TransitionParcelStatusUseCase transitionUseCase,
            com.laboussole.domain.port.in.IssueLandTitleUseCase issueTitleUseCase,
            com.laboussole.application.service.PdfGenerationService pdfService) {
        this.registerUseCase = registerUseCase;
        this.listUseCase = listUseCase;
        this.getUseCase = getUseCase;
        this.verifyUseCase = verifyUseCase;
        this.transitionUseCase = transitionUseCase;
        this.issueTitleUseCase = issueTitleUseCase;
        this.pdfService = pdfService;
    }

    @Operation(summary = "List parcels (most recent first). Defaults to 100, max 500.")
    @GetMapping
    public List<ParcelResponse> list(
            @RequestParam(value = "ownerUserId", required = false) String ownerUserId,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        var owner = ownerUserId == null || ownerUserId.isBlank()
                ? Optional.<UserId>empty()
                : Optional.of(UserId.of(ownerUserId));
        return listUseCase.execute(new ListLandParcelsUseCase.Query(owner, limit)).stream()
                .map(ParcelResponse::from)
                .toList();
    }

    @Operation(summary = "Get a parcel by id.")
    @GetMapping("/{id}")
    public ParcelResponse get(@PathVariable("id") String id) {
        var parcel = getUseCase.execute(ParcelId.of(id));
        return ParcelResponse.from(parcel);
    }

    @Operation(summary = "Register a new parcel. The authenticated user becomes the owner of record.")
    @PostMapping
    public ResponseEntity<ParcelResponse> register(
            @Valid @RequestBody RegisterParcelRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        
        var documents = request.documents().stream()
                .map(d -> new RegisterLandParcelUseCase.DocumentCommand(
                        d.type(),
                        d.label(),
                        d.storageKey(),
                        d.fileName()))
                .toList();

        var parcel = registerUseCase.execute(new RegisterLandParcelUseCase.Command(
                request.reference(),
                request.name(),
                request.regionLabel(),
                request.ownerLabel(),
                principal.userId(),
                request.areaHectares(),
                request.estimatedValueXof(),
                request.geometry().toDomain(),
                documents));
        return ResponseEntity.status(HttpStatus.CREATED).body(ParcelResponse.from(parcel));
    }

    @Operation(summary = "Mark a parcel as verified and certified. Cadastral officers and admins only.")
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    @PostMapping("/{id}/verify")
    public ParcelResponse verify(
            @PathVariable("id") String id,
            @Valid @RequestBody VerifyParcelRequest request) {
        var parcel = verifyUseCase.execute(new VerifyLandParcelUseCase.Command(
                ParcelId.of(id),
                request.newTrustScore()));
        return ParcelResponse.from(parcel);
    }

    @Operation(summary = "Transition parcel status. Officers and Admins only.")
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    @PostMapping("/{id}/transition")
    public ParcelResponse transition(
            @PathVariable("id") String id,
            @RequestParam("status") com.laboussole.domain.model.parcel.ParcelStatus status) {
        var parcel = transitionUseCase.execute(new com.laboussole.domain.port.in.TransitionParcelStatusUseCase.Command(
                ParcelId.of(id),
                status));
        return ParcelResponse.from(parcel);
    }

    @Operation(summary = "Issue a Land Title (TF) for a certified parcel. Officers and Admins only.")
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    @PostMapping("/{id}/issue-title")
    public ParcelResponse issueTitle(@PathVariable("id") String id) {
        var parcel = issueTitleUseCase.execute(new com.laboussole.domain.port.in.IssueLandTitleUseCase.Command(ParcelId.of(id)));
        return ParcelResponse.from(parcel);
    }

    @Operation(summary = "Download the official Land Title (TF) PDF.")
    @GetMapping("/{id}/title-pdf")
    public ResponseEntity<byte[]> downloadTitle(@PathVariable("id") String id) {
        var parcel = getUseCase.execute(ParcelId.of(id));
        if (parcel.status() != com.laboussole.domain.model.parcel.ParcelStatus.TITLE_ISSUED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        byte[] pdf = pdfService.generateLandTitle(parcel);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=Titre_Foncier_" + parcel.titleNumber() + ".pdf")
                .body(pdf);
    }
}
