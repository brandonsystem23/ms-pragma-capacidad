package com.pragma.capacidad_service.domain.model.command;

import java.util.List;

public record CapabilityCommand (
        String name,
        String description,
        List<Long> technologyIds){
}
