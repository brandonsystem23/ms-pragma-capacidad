package com.pragma.capacidad_service.application.dto.request;

import java.util.List;

public record CapabilityRequest(
        String name,
        String description,
        List<Long> technologyIds
) {
}
