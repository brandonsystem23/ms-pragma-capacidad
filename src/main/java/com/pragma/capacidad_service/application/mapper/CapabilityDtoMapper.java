package com.pragma.capacidad_service.application.mapper;

import com.pragma.capacidad_service.application.dto.request.CapabilityRequest;
import com.pragma.capacidad_service.application.dto.response.CapabilityResponse;
import com.pragma.capacidad_service.domain.model.Capability;
import com.pragma.capacidad_service.domain.model.CapabilityCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CapabilityDtoMapper {

    CapabilityCommand toCommand(CapabilityRequest request);

    @Mapping(
            target = "numberTechnologies",
            expression = "java(capability.getTechnologies() != null ? capability.getTechnologies().size() : 0L)"
    )
    CapabilityResponse toResponse(Capability capability);
}
