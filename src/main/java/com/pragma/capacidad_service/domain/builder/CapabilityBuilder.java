package com.pragma.capacidad_service.domain.builder;

import com.pragma.capacidad_service.domain.model.Capability;
import com.pragma.capacidad_service.domain.model.CapabilityCommand;
import com.pragma.capacidad_service.domain.model.Technology;

public final class CapabilityBuilder {

    private CapabilityBuilder() {

    }

    public static Capability buildCapability(CapabilityCommand capabilityCommand) {
        return Capability.builder()
                .name(capabilityCommand.name())
                .description(capabilityCommand.description())
                .technologies(capabilityCommand.technologyIds().stream()
                        .map(CapabilityBuilder::buildTechnology).toList())
                .build();
    }

    public static Technology buildTechnology(Long id) {
        return Technology.builder()
                .id(id)
                .build();
    }
}
