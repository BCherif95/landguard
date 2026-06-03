package com.laboussole.application.usecase.monitoring;

import com.laboussole.domain.model.parcel.ParcelId;
import com.laboussole.domain.port.in.CalculateMovementScoreUseCase;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class CalculateMovementScoreService implements CalculateMovementScoreUseCase {
    private final Random random = new Random();

    @Override
    public int execute(ParcelId parcelId) {
        // Simulated logic for movement score calculation
        return random.nextInt(101);
    }
}
