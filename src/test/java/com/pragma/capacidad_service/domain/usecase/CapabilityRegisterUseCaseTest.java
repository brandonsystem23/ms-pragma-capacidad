package com.pragma.capacidad_service.domain.usecase;

import com.pragma.capacidad_service.domain.model.Capability;
import com.pragma.capacidad_service.domain.model.CapabilityCommand;
import com.pragma.capacidad_service.domain.spi.ICapabilityPersistencePort;
import com.pragma.capacidad_service.domain.validation.capability.CapabilityValidator;
import com.pragma.capacidad_service.domain.validation.capability.DomainCapabilityValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapabilityRegisterUseCaseTest {

    @Mock
    private ICapabilityPersistencePort iCapabilityPersistencePort;

    @Mock
    private DomainCapabilityValidator domainCapabilityValidator;

    @Mock
    private CapabilityValidator capabilityValidator;

    @InjectMocks
    private CapabilityRegisterUseCase capabilityRegisterUseCase;

    @Test
    void shouldCreateCapabilitySuccessfully() {

        String token = "Bearer token";

        CapabilityCommand command = new CapabilityCommand(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                List.of(1L, 2L, 3L)
        );

        Capability savedCapability = Capability.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .build();

        doNothing()
                .when(domainCapabilityValidator)
                .validateUserCommand(command);

        when(capabilityValidator.validateCapability(
                command.name(),
                command.technologyIds(),
                token
        )).thenReturn(Mono.empty());

        when(iCapabilityPersistencePort.save(
                ArgumentMatchers.any(Capability.class)
        )).thenReturn(Mono.just(savedCapability));

        StepVerifier.create(
                        capabilityRegisterUseCase.create(command, token)
                )
                .expectNext(savedCapability)
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenCapabilityValidationFails() {

        String token = "Bearer token";

        CapabilityCommand command = new CapabilityCommand(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                List.of(1L, 2L, 3L)
        );

        doNothing()
                .when(domainCapabilityValidator)
                .validateUserCommand(command);

        when(capabilityValidator.validateCapability(
                command.name(),
                command.technologyIds(),
                token
        )).thenReturn(
                Mono.error(new RuntimeException("error validando capacidad"))
        );

        StepVerifier.create(
                        capabilityRegisterUseCase.create(command, token)
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error validando capacidad")
                )
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenSaveFails() {

        String token = "Bearer token";

        CapabilityCommand command = new CapabilityCommand(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                List.of(1L, 2L, 3L)
        );

        doNothing()
                .when(domainCapabilityValidator)
                .validateUserCommand(command);

        when(capabilityValidator.validateCapability(
                command.name(),
                command.technologyIds(),
                token
        )).thenReturn(Mono.empty());

        when(iCapabilityPersistencePort.save(
                ArgumentMatchers.any(Capability.class)
        )).thenReturn(
                Mono.error(new RuntimeException("error guardando capacidad"))
        );

        StepVerifier.create(
                        capabilityRegisterUseCase.create(command, token)
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error guardando capacidad")
                )
                .verify();
    }
}