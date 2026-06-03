package com.laboussole.application.usecase;

import com.laboussole.domain.exception.ParcelNotFoundException;
import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.port.in.IssueLandTitleUseCase;
import com.laboussole.domain.port.out.LandParcelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class IssueLandTitleService implements IssueLandTitleUseCase {

    private final LandParcelRepository repository;
    // Simple sequence for demo purposes. In production, this would be a database sequence.
    private static final AtomicLong SEQUENCE = new AtomicLong(100);

    public IssueLandTitleService(LandParcelRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public LandParcel execute(Command command) {
        var parcel = repository.findById(command.parcelId())
                .orElseThrow(() -> new ParcelNotFoundException(command.parcelId()));
        
        String regionCode = getRegionCode(parcel.regionLabel());
        String year = String.valueOf(Year.now().getValue());
        String seq = String.format("%06d", SEQUENCE.incrementAndGet());
        
        String titleNumber = String.format("TF-ML-%s-%s-%s", regionCode, year, seq);
        
        parcel.issueTitle(titleNumber);
        return repository.save(parcel);
    }

    private String getRegionCode(String label) {
        if (label == null) return "BKO";
        String l = label.toUpperCase();
        if (l.contains("BAMAKO")) return "BKO";
        if (l.contains("KAYES")) return "KYS";
        if (l.contains("KOULIKORO")) return "KKR";
        if (l.contains("SIKASSO")) return "SKO";
        if (l.contains("SEGOU")) return "SGO";
        if (l.contains("MOPTI")) return "MPT";
        if (l.contains("GAO")) return "GAO";
        if (l.contains("TOMBOUCTOU")) return "TBK";
        if (l.contains("KIDAL")) return "KDL";
        return "BKO"; // Default to Bamako
    }
}
