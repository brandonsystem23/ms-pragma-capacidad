package com.pragma.capacidad_service.domain.api;

import reactor.core.publisher.Flux;

import java.util.List;

public interface ICapabilityExistsByIdsServicePort {

    Flux<Long> retrieveExistingIds(List<Long> ids);
}
