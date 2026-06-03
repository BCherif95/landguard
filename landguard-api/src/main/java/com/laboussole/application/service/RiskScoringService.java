package com.laboussole.application.service;

import com.laboussole.domain.model.certification.LandTitle;
import com.laboussole.domain.model.parcel.LandParcel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiskScoringService {

    public record Scores(int authenticity, int fraud, int legal) {}

    public Scores calculate(LandParcel parcel, LandTitle title, List<String> fraudAlerts) {
        int authenticity = 100;
        int fraud = 0;
        int legal = 0;

        // Base authenticity on fraud alerts
        authenticity -= fraudAlerts.size() * 20;

        // Fraud score based on critical alerts
        for (String alert : fraudAlerts) {
            if (alert.contains("Format") || alert.contains("surface")) {
                fraud += 30;
            }
        }

        // Legal score
        if (title.titleStatus().equalsIgnoreCase("LITIGE")) {
            legal += 80;
            authenticity -= 40;
        }

        return new Scores(
                Math.max(0, authenticity),
                Math.min(100, fraud),
                Math.min(100, legal)
        );
    }
}
