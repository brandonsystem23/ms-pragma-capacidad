package com.pragma.capacidad_service.domain.api;

import com.pragma.capacidad_service.domain.model.Capability;
import com.pragma.capacidad_service.domain.model.command.CapabilityCommand;
import reactor.core.publisher.Mono;

public interface ICapabilityRegisterServicePort {

    Mono<Capability> create(CapabilityCommand capabilityCommand, String token);
}
