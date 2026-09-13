package com.pragma.capacidad_service.infrastructure.out.webclient.dto;

import lombok.Builder;

@Builder
public record TechnologyDetailResponse(
        Long id,
        String name,
        String description
) {
}
