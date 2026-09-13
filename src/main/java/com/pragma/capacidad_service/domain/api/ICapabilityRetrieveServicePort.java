package com.pragma.capacidad_service.domain.api;

import com.pragma.capacidad_service.domain.model.Capability;
import com.pragma.capacidad_service.domain.model.command.CapabilityPageCommand;
import com.pragma.capacidad_service.domain.model.PagedResult;
import reactor.core.publisher.Mono;

public interface ICapabilityRetrieveServicePort {

    Mono<PagedResult<Capability>> getCapabilities(CapabilityPageCommand command, String token);
}
