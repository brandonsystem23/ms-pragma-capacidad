package com.pragma.capacidad_service.domain.usecase;

import com.pragma.capacidad_service.domain.api.ICapabilityRetrieveServicePort;
import com.pragma.capacidad_service.domain.model.Capability;
import com.pragma.capacidad_service.domain.model.PagedResult;
import com.pragma.capacidad_service.domain.model.command.CapabilityPageCommand;
import com.pragma.capacidad_service.domain.service.TechnologyDetailService;
import com.pragma.capacidad_service.domain.spi.ICapabilityPersistencePort;
import com.pragma.capacidad_service.domain.validation.capability.DomainCapabilityValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class CapabilityRetrieveUseCase
        implements ICapabilityRetrieveServicePort {

    private final ICapabilityPersistencePort iCapabilityPersistencePort;
    private final DomainCapabilityValidator domainCapabilityValidator;
    private final TechnologyDetailService technologyDetailService;

    @Override
    public Mono<PagedResult<Capability>> getCapabilities(
            CapabilityPageCommand command,
            String token
    ) {
        return Mono.defer(() -> {
            domainCapabilityValidator.validatePagination(command);

            return iCapabilityPersistencePort.findAll(
                    command.page(),
                    command.size(),
                    command.sortBy(),
                    command.direction()
            );
        }).flatMap(pagedResult ->
                Flux.fromIterable(pagedResult.content())
                        .concatMap(capability ->
                                enrichCapability(capability, token)
                        )
                        .collectList()
                        .map(capabilities ->
                                PagedResult.<Capability>builder()
                                        .content(capabilities)
                                        .page(pagedResult.page())
                                        .size(pagedResult.size())
                                        .totalElements(pagedResult.totalElements())
                                        .totalPages(pagedResult.totalPages())
                                        .first(pagedResult.first())
                                        .last(pagedResult.last())
                                        .build()
                        )
        );
    }

    private Mono<Capability> enrichCapability(Capability capability, String token) {

        log.info(
                "Capability {} - technologies: {}",
                capability.getId(),
                capability.getTechnologies()
        );
        return technologyDetailService.enrich(capability.getTechnologies(), token)
                .map(technologies -> {
                    capability.setTechnologies(technologies);
                    return capability;
                });
    }
}