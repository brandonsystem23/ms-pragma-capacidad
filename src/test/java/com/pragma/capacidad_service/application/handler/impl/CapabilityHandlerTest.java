package com.pragma.capacidad_service.application.handler.impl;

import com.pragma.capacidad_service.application.dto.request.CapabilityRequest;
import com.pragma.capacidad_service.application.dto.response.CapabilityListItemResponse;
import com.pragma.capacidad_service.application.dto.response.CapabilityResponse;
import com.pragma.capacidad_service.application.dto.response.TechnologyBasicResponse;
import com.pragma.capacidad_service.application.mapper.CapabilityDtoMapper;
import com.pragma.capacidad_service.domain.api.ICapabilityExistsByIdsServicePort;
import com.pragma.capacidad_service.domain.api.ICapabilityRegisterServicePort;
import com.pragma.capacidad_service.domain.api.ICapabilityRetrieveServicePort;
import com.pragma.capacidad_service.domain.model.Capability;
import com.pragma.capacidad_service.domain.model.PagedResult;
import com.pragma.capacidad_service.domain.model.Technology;
import com.pragma.capacidad_service.domain.model.command.CapabilityCommand;
import com.pragma.capacidad_service.domain.model.command.CapabilityPageCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapabilityHandlerTest {

    @Mock
    private ICapabilityRegisterServicePort iCapabilityRegisterServicePort;

    @Mock
    private ICapabilityRetrieveServicePort iCapabilityRetrieveServicePort;

    @Mock
    private ICapabilityExistsByIdsServicePort iCapabilityExistsByIdsServicePort;

    @Mock
    private CapabilityDtoMapper capabilityDtoMapper;


    @InjectMocks
    private CapabilityHandler capabilityHandler;

    @Test
    void shouldCreateCapabilityAndMapResponse() {

        String token = "Bearer token";

        CapabilityRequest request = new CapabilityRequest(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                List.of(1L, 2L, 3L)
        );

        CapabilityCommand command = new CapabilityCommand(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                List.of(1L, 2L, 3L)
        );

        Capability capability = Capability.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .technologies(List.of())
                .build();

        CapabilityResponse response = CapabilityResponse.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .numberTechnologies(3L)
                .build();

        when(capabilityDtoMapper.toCommand(request))
                .thenReturn(command);

        when(iCapabilityRegisterServicePort.create(command, token))
                .thenReturn(Mono.just(capability));

        when(capabilityDtoMapper.toResponse(capability))
                .thenReturn(response);

        StepVerifier.create(capabilityHandler.create(request, token))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenCreateFails() {

        String token = "Bearer token";

        CapabilityRequest request = new CapabilityRequest(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                List.of(1L, 2L, 3L)
        );

        CapabilityCommand command = new CapabilityCommand(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                List.of(1L, 2L, 3L)
        );

        when(capabilityDtoMapper.toCommand(request))
                .thenReturn(command);

        when(iCapabilityRegisterServicePort.create(command, token))
                .thenReturn(Mono.error(
                        new RuntimeException("error creando capacidad")
                ));

        StepVerifier.create(capabilityHandler.create(request, token))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error creando capacidad")
                )
                .verify();
    }

    @Test
    void shouldGetCapabilitiesAndMapPagedResponse() {
        String token = "token";

        Capability capability = Capability.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .technologies(List.of(
                        Technology.builder().id(1L).name("Java").build(),
                        Technology.builder().id(2L).name("Spring").build()
                ))
                .build();

        PagedResult<Capability> pagedResult = PagedResult.<Capability>builder()
                .content(List.of(capability))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        CapabilityListItemResponse itemResponse = CapabilityListItemResponse.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .technologies(List.of(
                        TechnologyBasicResponse.builder().id(1L).name("Java").build(),
                        TechnologyBasicResponse.builder().id(2L).name("Spring").build()
                ))
                .build();

        when(iCapabilityRetrieveServicePort.getCapabilities(
                new CapabilityPageCommand(0, 10, "name", "asc"),
                token
        )).thenReturn(Mono.just(pagedResult));

        when(capabilityDtoMapper.toListItemResponse(capability))
                .thenReturn(itemResponse);

        StepVerifier.create(
                        capabilityHandler.getCapabilities(0, 10, "name", "asc", token)
                )
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertEquals(1, response.content().size());
                    org.junit.jupiter.api.Assertions.assertEquals(0, response.page());
                    org.junit.jupiter.api.Assertions.assertEquals(10, response.size());
                    org.junit.jupiter.api.Assertions.assertEquals(1, response.totalElements());
                    org.junit.jupiter.api.Assertions.assertEquals(1, response.totalPages());
                    org.junit.jupiter.api.Assertions.assertTrue(response.first());
                    org.junit.jupiter.api.Assertions.assertTrue(response.last());
                    org.junit.jupiter.api.Assertions.assertEquals("Backend", response.content().get(0).name());
                    org.junit.jupiter.api.Assertions.assertEquals(2, response.content().get(0).technologies().size());
                })
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenGetCapabilitiesFails() {
        String token = "token";

        when(iCapabilityRetrieveServicePort.getCapabilities(
                new CapabilityPageCommand(0, 10, "name", "asc"),
                token
        )).thenReturn(Mono.error(new RuntimeException("error listando capacidades")));

        StepVerifier.create(
                        capabilityHandler.getCapabilities(0, 10, "name", "asc", token)
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error listando capacidades")
                )
                .verify();
    }

    @Test
    void shouldReturnExistingTechnologyIds() {
        List<Long> ids = List.of(1L, 2L, 3L);
        List<Long> existingIds = List.of(1L, 3L);

        when(iCapabilityExistsByIdsServicePort.retrieveExistingIds(ids))
                .thenReturn(Flux.fromIterable(existingIds));

        StepVerifier.create(capabilityHandler.existsByIds(ids))
                .expectNext(existingIds)
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenExistsByIdsFails() {
        List<Long> ids = List.of(1L, 2L, 3L);

        when(iCapabilityExistsByIdsServicePort.retrieveExistingIds(ids))
                .thenReturn(Flux.error(
                        new RuntimeException("error buscando tecnologías existentes")
                ));

        StepVerifier.create(capabilityHandler.existsByIds(ids))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error buscando tecnologías existentes")
                )
                .verify();
    }

    @Test
    void shouldFindCapabilitiesByIdsAndMapResponse() {
        List<Long> ids = List.of(1L, 2L);
        String token = "Bearer token";

        Capability capability1 = Capability.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .technologies(List.of())
                .build();

        Capability capability2 = Capability.builder()
                .id(2L)
                .name("Frontend")
                .description("Capacidad frontend")
                .technologies(List.of())
                .build();

        CapabilityListItemResponse response1 = CapabilityListItemResponse.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .technologies(List.of())
                .build();

        CapabilityListItemResponse response2 = CapabilityListItemResponse.builder()
                .id(2L)
                .name("Frontend")
                .description("Capacidad frontend")
                .technologies(List.of())
                .build();

        when(iCapabilityRetrieveServicePort.retrieveByIds(ids, token))
                .thenReturn(Flux.just(capability1, capability2));

        when(capabilityDtoMapper.toListItemResponse(capability1))
                .thenReturn(response1);

        when(capabilityDtoMapper.toListItemResponse(capability2))
                .thenReturn(response2);

        StepVerifier.create(capabilityHandler.findByIds(ids, token))
                .expectNext(response1)
                .expectNext(response2)
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenFindByIdsFails() {
        List<Long> ids = List.of(1L, 2L);
        String token = "Bearer token";

        when(iCapabilityRetrieveServicePort.retrieveByIds(ids, token))
                .thenReturn(Flux.error(
                        new RuntimeException("error buscando capacidades")
                ));

        StepVerifier.create(capabilityHandler.findByIds(ids, token))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error buscando capacidades")
                )
                .verify();
    }

}