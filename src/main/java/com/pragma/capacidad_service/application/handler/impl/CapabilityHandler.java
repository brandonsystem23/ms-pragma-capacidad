package com.pragma.capacidad_service.application.handler.impl;

import com.pragma.capacidad_service.application.dto.request.CapabilityRequest;
import com.pragma.capacidad_service.application.dto.response.CapabilityListItemResponse;
import com.pragma.capacidad_service.application.dto.response.CapabilityResponse;
import com.pragma.capacidad_service.application.dto.response.PagedCapabilityResponse;
import com.pragma.capacidad_service.application.handler.ICapabilityHandler;
import com.pragma.capacidad_service.application.mapper.CapabilityDtoMapper;
import com.pragma.capacidad_service.domain.api.ICapabilityExistsByIdsServicePort;
import com.pragma.capacidad_service.domain.api.ICapabilityRegisterServicePort;
import com.pragma.capacidad_service.domain.api.ICapabilityRetrieveServicePort;
import com.pragma.capacidad_service.domain.model.command.CapabilityPageCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CapabilityHandler implements ICapabilityHandler {

    private final ICapabilityRegisterServicePort iCapabilityRegisterServicePort;
    private final ICapabilityRetrieveServicePort iCapabilityRetrieveServicePort;
    private final ICapabilityExistsByIdsServicePort iCapabilityExistsByIdsServicePort;
    private final CapabilityDtoMapper capabilityDtoMapper;

    @Override
    public Mono<CapabilityResponse> create(CapabilityRequest request, String token) {
        return iCapabilityRegisterServicePort
                .create(capabilityDtoMapper.toCommand(request), token)
                .map(capabilityDtoMapper::toResponse);
    }

    @Override
    public Mono<PagedCapabilityResponse> getCapabilities(int page, int size, String sortBy, String direction,
                                                         String token) {

        CapabilityPageCommand command = new CapabilityPageCommand(page, size, sortBy, direction);

        return iCapabilityRetrieveServicePort
                .getCapabilities(command, token)
                .map(result -> PagedCapabilityResponse.builder()
                        .content(
                                result.content()
                                        .stream()
                                        .map(capabilityDtoMapper::toListItemResponse)
                                        .toList()
                        )
                        .page(result.page())
                        .size(result.size())
                        .totalElements(result.totalElements())
                        .totalPages(result.totalPages())
                        .first(result.first())
                        .last(result.last())
                        .build()
                );
    }

    @Override
    public Mono<List<Long>> existsByIds(List<Long> ids) {
        return iCapabilityExistsByIdsServicePort.retrieveExistingIds(ids)
                .collectList();
    }

    @Override
    public Flux<CapabilityListItemResponse> findByIds(List<Long> ids, String token) {
        return iCapabilityRetrieveServicePort.retrieveByIds(ids, token)
                .map(capabilityDtoMapper::toListItemResponse);
    }
}