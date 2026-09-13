package com.pragma.capacidad_service.infrastructure.input.rest;

import com.pragma.capacidad_service.application.dto.request.CapabilityRequest;
import com.pragma.capacidad_service.application.dto.response.CapabilityListItemResponse;
import com.pragma.capacidad_service.application.dto.response.CapabilityResponse;
import com.pragma.capacidad_service.application.dto.response.PagedCapabilityResponse;
import com.pragma.capacidad_service.application.dto.response.TechnologyBasicResponse;
import com.pragma.capacidad_service.application.handler.ICapabilityHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapabilityControllerTest {

    @Mock
    private ICapabilityHandler iCapabilityHandler;

    @InjectMocks
    private CapabilityController capabilityController;

    @Test
    void shouldCreateCapabilitySuccessfully() {

        String authorizationHeader = "Bearer token";
        String token = "token";

        CapabilityRequest request = new CapabilityRequest(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                java.util.List.of(1L, 2L, 3L)
        );

        CapabilityResponse response = CapabilityResponse.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .numberTechnologies(3L)
                .build();

        when(iCapabilityHandler.create(request, token))
                .thenReturn(Mono.just(response));

        StepVerifier.create(
                        capabilityController.createTechnology(
                                authorizationHeader,
                                request
                        )
                )
                .expectNext(response)
                .verifyComplete();

        verify(iCapabilityHandler).create(request, token);
    }

    @Test
    void shouldPropagateErrorWhenCreateFails() {

        String authorizationHeader = "Bearer token";
        String token = "token";

        CapabilityRequest request = new CapabilityRequest(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                java.util.List.of(1L, 2L, 3L)
        );

        when(iCapabilityHandler.create(request, token))
                .thenReturn(
                        Mono.error(
                                new RuntimeException("error creando capacidad")
                        )
                );

        StepVerifier.create(
                        capabilityController.createTechnology(
                                authorizationHeader,
                                request
                        )
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error creando capacidad")
                )
                .verify();

        verify(iCapabilityHandler).create(request, token);
    }

    @Test
    void shouldExtractTokenFromAuthorizationHeader() {

        String authorizationHeader = "Bearer abc123";
        String token = "abc123";

        CapabilityRequest request = new CapabilityRequest(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                java.util.List.of(1L, 2L, 3L)
        );

        CapabilityResponse response = CapabilityResponse.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .numberTechnologies(3L)
                .build();

        when(iCapabilityHandler.create(request, token))
                .thenReturn(Mono.just(response));

        StepVerifier.create(
                        capabilityController.createTechnology(
                                authorizationHeader,
                                request
                        )
                )
                .expectNext(response)
                .verifyComplete();

        verify(iCapabilityHandler).create(request, token);
    }

    @Test
    void shouldGetCapabilitiesSuccessfully() {
        String authorizationHeader = "Bearer token";
        String token = "token";

        PagedCapabilityResponse response = PagedCapabilityResponse.builder()
                .content(java.util.List.of(
                        CapabilityListItemResponse.builder()
                                .id(1L)
                                .name("Backend")
                                .description("Capacidad backend")
                                .technologies(java.util.List.of(
                                        TechnologyBasicResponse.builder().id(1L).name("Java").build(),
                                        TechnologyBasicResponse.builder().id(2L).name("Spring").build()
                                ))
                                .build()
                ))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(iCapabilityHandler.getCapabilities(0, 10, "name", "asc", token))
                .thenReturn(Mono.just(response));

        StepVerifier.create(
                        capabilityController.getCapabilities(
                                authorizationHeader,
                                0,
                                10,
                                "name",
                                "asc"
                        )
                )
                .expectNext(response)
                .verifyComplete();

        verify(iCapabilityHandler).getCapabilities(0, 10, "name", "asc", token);
    }

    @Test
    void shouldPropagateErrorWhenGetCapabilitiesFails() {
        String authorizationHeader = "Bearer token";
        String token = "token";

        when(iCapabilityHandler.getCapabilities(0, 10, "name", "asc", token))
                .thenReturn(Mono.error(new RuntimeException("error listando capacidades")));

        StepVerifier.create(
                        capabilityController.getCapabilities(
                                authorizationHeader,
                                0,
                                10,
                                "name",
                                "asc"
                        )
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error listando capacidades")
                )
                .verify();

        verify(iCapabilityHandler).getCapabilities(0, 10, "name", "asc", token);
    }


    @Test
    void shouldReturnExistingTechnologyIdsSuccessfully() {
        List<Long> ids = List.of(1L, 2L, 3L);
        List<Long> response  = List.of(1L,3L);

        when(iCapabilityHandler.existsByIds(ids))
                .thenReturn(Mono.just(response));

        StepVerifier.create(capabilityController.existsByIds(ids))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenExistsByIdsFails() {
        List<Long> ids = List.of(1L, 2L, 3L);

        when(iCapabilityHandler.existsByIds(ids))
                .thenReturn(Mono.error(
                        new RuntimeException("error validando tecnologías por ids")
                ));

        StepVerifier.create(capabilityController.existsByIds(ids))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error validando tecnologías por ids"))
                .verify();
    }

}