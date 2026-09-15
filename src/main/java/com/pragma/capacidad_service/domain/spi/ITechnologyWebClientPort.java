package com.pragma.capacidad_service.domain.spi;

import com.pragma.capacidad_service.domain.model.Technology;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ITechnologyWebClientPort {

    Mono<List<Long>> existsByIds(List<Long> ids, String token);

    Mono<List<Technology>> findByIds(List<Long> ids, String token);

    Mono<Void> deleteByIds(List<Long> ids, String token);
}
