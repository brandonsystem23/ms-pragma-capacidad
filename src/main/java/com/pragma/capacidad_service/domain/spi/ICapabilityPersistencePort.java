package com.pragma.capacidad_service.domain.spi;

import com.pragma.capacidad_service.domain.model.Capability;
import reactor.core.publisher.Mono;

public interface ICapabilityPersistencePort {

    Mono<Capability> save(Capability capability);

    Mono<Boolean> existsByName(String name);

}
