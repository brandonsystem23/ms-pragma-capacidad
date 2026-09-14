package com.pragma.capacidad_service.application.handler;

import com.pragma.capacidad_service.application.dto.request.CapabilityRequest;
import com.pragma.capacidad_service.application.dto.response.CapabilityListItemResponse;
import com.pragma.capacidad_service.application.dto.response.CapabilityResponse;
import com.pragma.capacidad_service.application.dto.response.PagedCapabilityResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICapabilityHandler {

    Mono<CapabilityResponse> create(CapabilityRequest request, String token);

    Mono<PagedCapabilityResponse> getCapabilities(
            int page,
            int size,
            String sortBy,
            String direction,
            String token
    );

    Mono<List<Long>> existsByIds(List<Long> ids);

    Flux<CapabilityListItemResponse> findByIds(List<Long> ids, String token);
}
