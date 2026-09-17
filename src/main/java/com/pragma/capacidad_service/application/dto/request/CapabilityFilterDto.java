package com.pragma.capacidad_service.application.dto.request;

public record CapabilityFilterDto(
        int page,
        int size,
        String sortBy,
        String direction
) {
}
