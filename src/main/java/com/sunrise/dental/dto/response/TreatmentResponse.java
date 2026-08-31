package com.sunrise.dental.dto.response;

public record TreatmentResponse(
        Long id,
        String code,
        String name,
        String description,
        String category,
        Double cost,
        int durationMinutes,
        boolean active
) {
}
