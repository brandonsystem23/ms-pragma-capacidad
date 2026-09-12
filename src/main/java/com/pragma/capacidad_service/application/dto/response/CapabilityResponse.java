package com.pragma.capacidad_service.application.dto.response;

import lombok.Builder;

@Builder
public record CapabilityResponse(

        Long id,

        String name,

        String description,

        Long numberTechnologies
) {
}
