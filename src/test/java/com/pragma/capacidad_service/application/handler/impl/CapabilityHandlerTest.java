package com.pragma.capacidad_service.application.handler.impl;

import com.pragma.capacidad_service.application.dto.request.CapabilityRequest;
import com.pragma.capacidad_service.application.dto.response.CapabilityResponse;
import com.pragma.capacidad_service.application.mapper.CapabilityDtoMapper;
import com.pragma.capacidad_service.domain.api.ICapabilityRegisterServicePort;
import com.pragma.capacidad_service.domain.model.Capability;
import com.pragma.capacidad_service.domain.model.CapabilityCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapabilityHandlerTest {

    @Mock
    private ICapabilityRegisterServicePort iCapabilityRegisterServicePort;

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
}