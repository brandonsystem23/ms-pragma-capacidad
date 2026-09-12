package com.pragma.capacidad_service.domain.validation;

import com.pragma.capacidad_service.domain.exception.DomainErrorCode;
import com.pragma.capacidad_service.domain.exception.DomainErrorMessages;
import com.pragma.capacidad_service.domain.exception.DomainException;
import com.pragma.capacidad_service.domain.spi.ICapabilityPersistencePort;
import com.pragma.capacidad_service.domain.spi.ITechnologyWebClientPort;
import com.pragma.capacidad_service.domain.validation.capability.CapabilityValidator;
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
class CapabilityValidatorTest {

    @Mock
    private ICapabilityPersistencePort iCapabilityPersistencePort;

    @Mock
    private ITechnologyWebClientPort iTechnologyWebClientPort;

    @InjectMocks
    private CapabilityValidator capabilityValidator;

    @Test
    void shouldCompleteWhenCapabilityIsValid() {

        String name = "Desarrollo Backend";
        List<Long> technologyIds = List.of(1L, 2L, 3L);
        String token = "Bearer token";

        when(iCapabilityPersistencePort.existsByName(name))
                .thenReturn(Mono.just(false));

        when(iTechnologyWebClientPort.existsByIds(technologyIds, token))
                .thenReturn(Mono.just(technologyIds));

        StepVerifier.create(
                        capabilityValidator.validateCapability(
                                name,
                                technologyIds,
                                token
                        )
                )
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenCapabilityNameAlreadyExists() {

        String name = "Desarrollo Backend";
        List<Long> technologyIds = List.of(1L, 2L, 3L);
        String token = "Bearer token";

        when(iCapabilityPersistencePort.existsByName(name))
                .thenReturn(Mono.just(true));

        StepVerifier.create(
                        capabilityValidator.validateCapability(
                                name,
                                technologyIds,
                                token
                        )
                )
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.DUPLICATE_NAME &&
                                error.getMessage().equals(DomainErrorMessages.DUPLICATE_NAME)
                )
                .verify();
    }

    @Test
    void shouldReturnErrorWhenTechnologyIdsDoNotExist() {

        String name = "Desarrollo Backend";
        List<Long> technologyIds = List.of(1L, 2L, 3L);
        List<Long> existingTechnologyIds = List.of(1L, 2L);
        String token = "Bearer token";

        when(iCapabilityPersistencePort.existsByName(name))
                .thenReturn(Mono.just(false));

        when(iTechnologyWebClientPort.existsByIds(technologyIds, token))
                .thenReturn(Mono.just(existingTechnologyIds));

        StepVerifier.create(
                        capabilityValidator.validateCapability(
                                name,
                                technologyIds,
                                token
                        )
                )
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.TECHNOLOGY_NOT_FOUNT &&
                                error.getMessage().equals(DomainErrorMessages.TECHNOLOGY_NOT_FOUND)
                )
                .verify();
    }

    @Test
    void shouldReturnErrorWhenTechnologyIdsAreDifferent() {

        String name = "Desarrollo Backend";
        List<Long> technologyIds = List.of(1L, 2L, 3L);
        List<Long> existingTechnologyIds = List.of(1L, 2L, 4L);
        String token = "Bearer token";

        when(iCapabilityPersistencePort.existsByName(name))
                .thenReturn(Mono.just(false));

        when(iTechnologyWebClientPort.existsByIds(technologyIds, token))
                .thenReturn(Mono.just(existingTechnologyIds));

        StepVerifier.create(
                        capabilityValidator.validateCapability(
                                name,
                                technologyIds,
                                token
                        )
                )
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.TECHNOLOGY_NOT_FOUNT &&
                                error.getMessage().equals(DomainErrorMessages.TECHNOLOGY_NOT_FOUND)
                )
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenCheckingCapabilityNameFails() {

        String name = "Desarrollo Backend";
        List<Long> technologyIds = List.of(1L, 2L, 3L);
        String token = "Bearer token";

        when(iCapabilityPersistencePort.existsByName(name))
                .thenReturn(Mono.error(
                        new RuntimeException("error consultando nombre")
                ));

        StepVerifier.create(
                        capabilityValidator.validateCapability(
                                name,
                                technologyIds,
                                token
                        )
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error consultando nombre")
                )
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenCheckingTechnologyIdsFails() {

        String name = "Desarrollo Backend";
        List<Long> technologyIds = List.of(1L, 2L, 3L);
        String token = "Bearer token";

        when(iCapabilityPersistencePort.existsByName(name))
                .thenReturn(Mono.just(false));

        when(iTechnologyWebClientPort.existsByIds(technologyIds, token))
                .thenReturn(Mono.error(
                        new RuntimeException("error consultando tecnologías")
                ));

        StepVerifier.create(
                        capabilityValidator.validateCapability(
                                name,
                                technologyIds,
                                token
                        )
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error consultando tecnologías")
                )
                .verify();
    }
}