package com.laboussole.interfaces.rest.parcel.dto;

public record DocumentDto(
        String type,
        String label,
        String storageKey,
        String fileName) {
}
