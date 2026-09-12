package com.pragma.capacidad_service.infrastructure.out.webclient.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record TechnologyRequest(
        List<Long> ids
) {
}
