package com.laboussole.application.service;

import com.laboussole.domain.model.certification.LandTitle;
import com.laboussole.domain.model.parcel.LandParcel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class FraudDetectionService {

    private static final Pattern TF_PATTERN = Pattern.compile("^TF-\\d{1,6}-[A-Z]{2,10}$");

    public List<String> analyze(LandParcel parcel, LandTitle title) {
        List<String> alerts = new ArrayList<>();

        // 1. TF Pattern Validation (Mali specific format: TF-number-RegionCode)
        if (title.tfNumber() != null && !TF_PATTERN.matcher(title.tfNumber()).matches()) {
            alerts.add("Format de Titre Foncier invalide pour le Mali (Attendu: TF-0000-XX)");
        }

        // 2. Surface area consistency check
        double parcelArea = parcel.area().value().doubleValue();
        double titleArea = title.area().value().doubleValue();
        double difference = Math.abs(parcelArea - titleArea);
        double threshold = titleArea * 0.05; // 5% tolerance

        if (difference > threshold) {
            alerts.add("Incohérence de surface détectée : Terrain (" + parcelArea + " ha) vs Titre (" + titleArea + " ha)");
        }

        // 3. Status Check
        if ("LITIGE".equalsIgnoreCase(title.titleStatus())) {
            alerts.add("Le Titre Foncier est marqué comme étant sous LITIGE dans le livre foncier");
        }

        return alerts;
    }
}
