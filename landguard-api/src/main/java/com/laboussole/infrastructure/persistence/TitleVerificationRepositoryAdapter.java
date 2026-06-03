package com.laboussole.infrastructure.persistence;

import com.laboussole.domain.model.certification.CertificationId;
import com.laboussole.domain.model.certification.TitleVerificationCase;
import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.out.TitleVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TitleVerificationRepositoryAdapter implements TitleVerificationRepository {

    private final TitleVerificationSpringDataRepository springDataRepository;

    @Override
    public void save(TitleVerificationCase verificationCase) {
        springDataRepository.save(TitleVerificationCaseJpaEntity.fromDomain(verificationCase));
    }

    @Override
    public Optional<TitleVerificationCase> findById(CertificationId id) {
        return springDataRepository.findById(id.value())
                .map(TitleVerificationCaseJpaEntity::toDomain);
    }

    @Override
    public Optional<TitleVerificationCase> findByParcelId(ParcelId parcelId) {
        return springDataRepository.findByParcelId(parcelId.value())
                .map(TitleVerificationCaseJpaEntity::toDomain);
    }

    @Override
    public Optional<TitleVerificationCase> findByReference(String caseReference) {
        return springDataRepository.findByCaseReference(caseReference)
                .map(TitleVerificationCaseJpaEntity::toDomain);
    }
}
