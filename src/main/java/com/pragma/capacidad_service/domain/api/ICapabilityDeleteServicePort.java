package com.pragma.capacidad_service.domain.api;

import reactor.core.publisher.Mono;

import java.util.List;

public interface ICapabilityDeleteServicePort {

    Mono<Void> deleteByIds(List<Long> capabilityIds, String token);
}
