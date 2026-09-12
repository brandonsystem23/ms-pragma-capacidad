package com.pragma.capacidad_service.infrastructure.out.mysql.mapper;

import com.pragma.capacidad_service.domain.model.Capability;
import com.pragma.capacidad_service.domain.model.Technology;
import com.pragma.capacidad_service.infrastructure.out.mysql.entity.CapabilityEntity;
import com.pragma.capacidad_service.infrastructure.out.mysql.entity.CapabilityTechnologyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CapabilityEntityMapper {

    @Mapping(target = "technologies", ignore = true)
    Capability toDomain(CapabilityEntity capabilityEntity);

    CapabilityEntity toEntity(Capability capability);

    @Mapping(target = "id", source = "capabilityTechnologyEntity.technologyId")
    Technology toTechnology(CapabilityTechnologyEntity capabilityTechnologyEntity);

}