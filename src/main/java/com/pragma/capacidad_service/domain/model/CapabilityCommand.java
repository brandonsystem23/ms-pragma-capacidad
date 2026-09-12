package com.pragma.capacidad_service.domain.model;

import java.util.List;

public record CapabilityCommand (
        String name,
        String description,
        List<Long> technologyIds){
}
