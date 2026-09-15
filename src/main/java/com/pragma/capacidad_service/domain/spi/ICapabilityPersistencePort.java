package com.pragma.capacidad_service.domain.spi;

import com.pragma.capacidad_service.domain.model.Capability;
import com.pragma.capacidad_service.domain.model.PagedResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICapabilityPersistencePort {

    Mono<Capability> save(Capability capability);

    Mono<Boolean> existsByName(String name);

    Mono<PagedResult<Capability>> findAll(int page, int size, String sortBy, String direction);

    Flux<Long> findExistingIds(List<Long> ids);

    Flux<Capability> findByIds(List<Long> ids);

    Flux<Long> findTechnologyIdsByCapabilityIds(List<Long> capabilityIds);

    Mono<Void> updateCapabilityTechnologiesStatusByCapabilityIds(List<Long> capabilityIds, Boolean status);

    Mono<Void> updateCapabilitiesStatusByIds(List<Long> capabilityIds, Boolean status);
}
