package com.pragma.capacidad_service.domain.model.command;

public record CapabilityPageCommand(
        int page,
        int size,
        String sortBy,
        String direction
) {
}
