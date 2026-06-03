package com.laboussole.application.service;

import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.certification.CertificationId;
import com.laboussole.domain.model.certification.LandTitle;
import com.laboussole.domain.model.certification.TitleVerificationCase;
import com.laboussole.domain.model.certification.TitleVerificationRequisition;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.out.TitleVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TitleVerificationWorkflowService {

    private final TitleVerificationRepository verificationRepository;

    @Transactional
    public TitleVerificationCase openCase(ParcelId parcelId, String caseRef, LandTitle title, UserId userId) {
        if (verificationRepository.findByParcelId(parcelId).isPresent()) {
            throw new IllegalStateException("Verification case already exists for this parcel");
        }
        var verificationCase = TitleVerificationCase.open(parcelId, caseRef, title, userId);
        verificationRepository.save(verificationCase);
        return verificationCase;
    }

    @Transactional
    public void submitForVerification(CertificationId id) {
        var caseObj = verificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Case not found"));
        caseObj.submitForVerification();
        verificationRepository.save(caseObj);
    }

    @Transactional
    public void startDomainControl(CertificationId id) {
        var caseObj = verificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Case not found"));
        caseObj.startDomainControl();
        verificationRepository.save(caseObj);
    }

    @Transactional
    public void recordRequisition(CertificationId id, TitleVerificationRequisition requisition) {
        var caseObj = verificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Case not found"));
        caseObj.recordRequisition(requisition);
        verificationRepository.save(caseObj);
    }

    @Transactional
    public void certify(CertificationId id, UserId authorityId) {
        var caseObj = verificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Case not found"));
        caseObj.certify(authorityId);
        verificationRepository.save(caseObj);
    }
}
