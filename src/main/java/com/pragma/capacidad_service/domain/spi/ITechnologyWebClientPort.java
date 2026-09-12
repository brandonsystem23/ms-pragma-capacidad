package com.pragma.capacidad_service.domain.spi;

import reactor.core.publisher.Mono;

import java.util.List;

public interface ITechnologyWebClientPort {

    Mono<List<Long>> existsByIds(List<Long> ids, String token);
}
