package com.pragma.capacidad_service.infrastructure.out.webclient.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record TechnologyResponse(
        List<Long> existingIds
) {
}
