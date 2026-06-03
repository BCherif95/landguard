package com.laboussole.interfaces.rest.parcel.dto;

import com.laboussole.domain.model.parcel.LandParcel;
import com.laboussole.domain.model.parcel.ParcelStatus;
import com.laboussole.domain.model.parcel.RiskLevel;

import java.math.BigDecimal;
import java.time.Instant;

public record ParcelResponse(
        String id,
        String reference,
        String name,
        String regionLabel,
        String ownerLabel,
        String ownerUserId,
        BigDecimal areaHectares,
        long estimatedValueXof,
        ParcelStatus status,
        RiskLevel riskLevel,
        int riskScore,
        int trustScore,
        String titleNumber,
        PolygonDto geometry,
        Centroid centroid,
        Instant createdAt,
        Instant updatedAt,
        Instant lastVerifiedAt) {

    public static ParcelResponse from(LandParcel parcel) {
        var centroid = computeCentroid(parcel);
        return new ParcelResponse(
                parcel.id().asString(),
                parcel.reference().value(),
                parcel.name(),
                parcel.regionLabel(),
                parcel.ownerLabel(),
                parcel.ownerUserId() == null ? null : parcel.ownerUserId().asString(),
                parcel.area().value(),
                parcel.estimatedValue().asLong(),
                parcel.status(),
                parcel.riskLevel(),
                parcel.riskScore(),
                parcel.trustScore(),
                parcel.titleNumber(),
                PolygonDto.fromDomain(parcel.geometry()),
                centroid,
                parcel.createdAt(),
                parcel.updatedAt(),
                parcel.lastVerifiedAt());
    }

    /** Cheap centroid: arithmetic mean of the outer ring (sufficient for marker placement). */
    private static Centroid computeCentroid(LandParcel parcel) {
        var ring = parcel.geometry().outerRing();
        // Skip the closing duplicate point.
        var n = ring.size() - 1;
        double lon = 0, lat = 0;
        for (int i = 0; i < n; i++) {
            lon += ring.get(i).longitude();
            lat += ring.get(i).latitude();
        }
        return new Centroid(lon / n, lat / n);
    }

    public record Centroid(double longitude, double latitude) {}
}
