package com.laboussole.interfaces.rest.heritage;

import com.laboussole.domain.model.heritage.HeirId;
import com.laboussole.domain.model.heritage.SuccessionPlanId;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.in.heritage.*;
import com.laboussole.domain.port.out.SuccessionRepository;
import com.laboussole.infrastructure.security.AuthenticatedPrincipal;
import com.laboussole.interfaces.rest.heritage.dto.AddHeirRequest;
import com.laboussole.interfaces.rest.heritage.dto.SuccessionPlanResponse;
import com.laboussole.interfaces.rest.heritage.dto.VoteRequest;
import com.laboussole.domain.port.out.heritage.SuccessionAuditEventRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/heritage")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Heritage", description = "Succession and inheritance management.")
@RequiredArgsConstructor
public class HeritageController {

    private final CreateSuccessionPlanUseCase createUseCase;
    private final AddHeirUseCase addHeirUseCase;
    private final SubmitSuccessionForVotingUseCase submitUseCase;
    private final VoteSuccessionPlanUseCase voteUseCase;
    private final AnchorSuccessionUseCase anchorUseCase;
    private final TransferOwnershipUseCase transferUseCase;
    private final SuccessionRepository successionRepository;
    private final SuccessionAuditEventRepository auditEventRepository;

    @Operation(summary = "Get all succession plans.")
    @GetMapping("/plans")
    public List<SuccessionPlanResponse> list() {
        return successionRepository.findAll().stream()
                .map(SuccessionPlanResponse::from)
                .toList();
    }

    @Operation(summary = "Get a succession plan by parcel id.")
    @GetMapping("/plans/parcel/{parcelId}")
    public SuccessionPlanResponse getByParcel(@PathVariable UUID parcelId) {
        return successionRepository.findByParcelId(ParcelId.of(parcelId))
                .map(SuccessionPlanResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("No succession plan for parcel: " + parcelId));
    }

    @Operation(summary = "Create a new succession plan for a parcel.")
    @PostMapping("/plans")
    public SuccessionPlanResponse create(@RequestParam UUID parcelId) {
        var plan = createUseCase.execute(ParcelId.of(parcelId));
        return SuccessionPlanResponse.from(plan);
    }

    @Operation(summary = "Add an heir to a succession plan.")
    @PostMapping("/plans/{id}/heirs")
    public SuccessionPlanResponse addHeir(
            @PathVariable UUID id,
            @Valid @RequestBody AddHeirRequest request) {
        var plan = addHeirUseCase.execute(new AddHeirUseCase.Command(
                SuccessionPlanId.of(id),
                request.fullName(),
                request.relation(),
                request.sharePercentage()
        ));
        return SuccessionPlanResponse.from(plan);
    }

    @Operation(summary = "Submit a succession plan for voting.")
    @PostMapping("/plans/{id}/submit")
    public void submit(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        submitUseCase.submit(SuccessionPlanId.of(id), principal.userId().value().toString());
    }

    @Operation(summary = "Cast a vote on a succession plan.")
    @PostMapping("/plans/{id}/vote")
    public void vote(
            @PathVariable UUID id,
            @Valid @RequestBody VoteRequest request,
            HttpServletRequest servletRequest) {
        voteUseCase.vote(
                SuccessionPlanId.of(id),
                HeirId.of(request.heirId()),
                request.approved(),
                servletRequest.getRemoteAddr()
        );
    }

    @Operation(summary = "Anchor a validated succession plan on the blockchain.")
    @PostMapping("/plans/{id}/anchor")
    public void anchor(@PathVariable UUID id) {
        anchorUseCase.execute(SuccessionPlanId.of(id));
    }

    @Operation(summary = "Transfer ownership based on an anchored succession plan.")
    @PostMapping("/plans/{id}/transfer")
    public void transfer(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        transferUseCase.transfer(SuccessionPlanId.of(id), principal.userId().value().toString());
    }

    @Operation(summary = "Get audit trail for a succession plan.")
    @GetMapping("/plans/{id}/audit")
    public List<com.laboussole.domain.model.heritage.SuccessionAuditEvent> getAuditTrail(@PathVariable UUID id) {
        return auditEventRepository.findBySuccessionPlanId(SuccessionPlanId.of(id));
    }
}
