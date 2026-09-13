package com.pragma.capacidad_service.application.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record CapabilityListItemResponse(
        Long id,
        String name,
        String description,
        List<TechnologyBasicResponse> technologies
) {
}
