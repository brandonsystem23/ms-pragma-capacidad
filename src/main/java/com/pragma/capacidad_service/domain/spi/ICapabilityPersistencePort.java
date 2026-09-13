package com.pragma.capacidad_service.domain.spi;

import com.pragma.capacidad_service.domain.model.Capability;
import com.pragma.capacidad_service.domain.model.PagedResult;
import reactor.core.publisher.Mono;


public interface ICapabilityPersistencePort {

    Mono<Capability> save(Capability capability);

    Mono<Boolean> existsByName(String name);

    Mono<PagedResult<Capability>> findAll(int page, int size, String sortBy, String direction);

}
