package com.pragma.capacidad_service.application.mapper;

import com.pragma.capacidad_service.application.dto.request.CapabilityFilterDto;
import com.pragma.capacidad_service.application.dto.request.CapabilityRequest;
import com.pragma.capacidad_service.application.dto.response.CapabilityListItemResponse;
import com.pragma.capacidad_service.application.dto.response.CapabilityResponse;
import com.pragma.capacidad_service.application.dto.response.TechnologyBasicResponse;
import com.pragma.capacidad_service.domain.model.Capability;
import com.pragma.capacidad_service.domain.model.Technology;
import com.pragma.capacidad_service.domain.model.command.CapabilityCommand;
import com.pragma.capacidad_service.domain.model.command.CapabilityPageCommand;
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

    CapabilityListItemResponse toListItemResponse(Capability capability);

    TechnologyBasicResponse toTechnologyBasicResponse(Technology technology);

    CapabilityPageCommand toCommandPage(CapabilityFilterDto dto);
}
