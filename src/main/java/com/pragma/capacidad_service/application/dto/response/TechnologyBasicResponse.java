package com.pragma.capacidad_service.application.dto.response;

import lombok.Builder;

@Builder
public record TechnologyBasicResponse(
        Long id,
        String name
) {
}
