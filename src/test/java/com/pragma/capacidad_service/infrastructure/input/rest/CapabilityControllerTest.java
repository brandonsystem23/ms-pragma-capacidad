package com.pragma.capacidad_service.infrastructure.input.rest;

import com.pragma.capacidad_service.application.dto.request.CapabilityRequest;
import com.pragma.capacidad_service.application.dto.response.CapabilityResponse;
import com.pragma.capacidad_service.application.handler.ICapabilityHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
}