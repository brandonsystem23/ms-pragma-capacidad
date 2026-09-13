package com.pragma.capacidad_service.domain.service;

import com.pragma.capacidad_service.domain.model.Technology;
import com.pragma.capacidad_service.domain.spi.ITechnologyWebClientPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import java.util.List;

@RequiredArgsConstructor
public class TechnologyDetailService {

    private final ITechnologyWebClientPort iTechnologyWebClientPort;

    public Mono<List<Technology>> enrich(List<Technology> technologies, String token) {

        List<Long> ids = technologies.stream()
                .map(Technology::getId)
                .distinct()
                .toList();

        return iTechnologyWebClientPort.findByIds(ids, token);
    }
}