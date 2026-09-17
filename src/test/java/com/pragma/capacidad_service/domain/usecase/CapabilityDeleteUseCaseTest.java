package com.pragma.capacidad_service.domain.usecase;

import com.pragma.capacidad_service.domain.exception.DomainErrorCode;
import com.pragma.capacidad_service.domain.exception.DomainErrorMessages;
import com.pragma.capacidad_service.domain.exception.DomainException;
import com.pragma.capacidad_service.domain.spi.ICapabilityPersistencePort;
import com.pragma.capacidad_service.domain.spi.ITechnologyWebClientPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapabilityDeleteUseCaseTest {

    @Mock
    private ICapabilityPersistencePort iCapabilityPersistencePort;

    @Mock
    private ITechnologyWebClientPort iTechnologyWebClientPort;

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private CapabilityDeleteUseCase capabilityDeleteUseCase;

    @Test
    void shouldSoftDeleteCapabilitiesAndTechnologiesSuccessfully() {
        List<Long> capabilityIds = List.of(1L, 2L);
        String token = "token";

        when(iCapabilityPersistencePort.findTechnologyIdsByCapabilityIds(capabilityIds))
                .thenReturn(Flux.just(10L, 20L, 30L));
        when(iCapabilityPersistencePort.updateCapabilityTechnologiesStatusByCapabilityIds(capabilityIds, false))
                .thenReturn(Mono.empty());
        when(iCapabilityPersistencePort.updateCapabilitiesStatusByIds(capabilityIds, false))
                .thenReturn(Mono.empty());
        when(iTechnologyWebClientPort.deleteByIds(List.of(10L, 20L, 30L), token))
                .thenReturn(Mono.empty());
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(capabilityDeleteUseCase.deleteByIds(capabilityIds, token))
                .verifyComplete();
    }

    @Test
    void shouldRollbackStatusesWhenRemoteDeleteFails() {
        List<Long> capabilityIds = List.of(1L);
        String token = "token";

        when(iCapabilityPersistencePort.findTechnologyIdsByCapabilityIds(capabilityIds))
                .thenReturn(Flux.just(10L));
        when(iCapabilityPersistencePort.updateCapabilityTechnologiesStatusByCapabilityIds(capabilityIds, false))
                .thenReturn(Mono.empty());
        when(iCapabilityPersistencePort.updateCapabilitiesStatusByIds(capabilityIds, false))
                .thenReturn(Mono.empty());
        when(iTechnologyWebClientPort.deleteByIds(List.of(10L), token))
                .thenReturn(Mono.error(new RuntimeException("rollback remoto")));
        when(iCapabilityPersistencePort.updateCapabilityTechnologiesStatusByCapabilityIds(capabilityIds, true))
                .thenReturn(Mono.empty());
        when(iCapabilityPersistencePort.updateCapabilitiesStatusByIds(capabilityIds, true))
                .thenReturn(Mono.empty());
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(capabilityDeleteUseCase.deleteByIds(capabilityIds, token))
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.INTERNAL_ERROR &&
                                error.getMessage().equals(DomainErrorMessages.CAPABILITY_DELETE_ROLLBACK_ERROR))
                .verify();
    }

    @Test
    void shouldReturnValidationErrorWhenIdsAreEmpty() {
        StepVerifier.create(capabilityDeleteUseCase.deleteByIds(List.of(), "token"))
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.VALIDATION_ERROR &&
                                error.getMessage().equals(DomainErrorMessages.DELETE_IDS_REQUIRED))
                .verify();
    }

    @Test
    void shouldMapUnexpectedErrorToInternalDomainException() {
        List<Long> capabilityIds = List.of(1L, 2L);
        String token = "token";

        when(iCapabilityPersistencePort.findTechnologyIdsByCapabilityIds(capabilityIds))
                .thenReturn(Flux.error(new RuntimeException("error inesperado")));

        StepVerifier.create(
                        capabilityDeleteUseCase.deleteByIds(capabilityIds, token)
                )
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.INTERNAL_ERROR &&
                                error.getMessage().equals(
                                        DomainErrorMessages.CAPABILITY_DELETE_ROLLBACK_ERROR
                                )
                )
                .verify();
    }

    @Test
    void shouldRetryRollbackWhenRollbackFails() {
        List<Long> capabilityIds = List.of(1L);
        String token = "token";

        when(iCapabilityPersistencePort.findTechnologyIdsByCapabilityIds(capabilityIds))
                .thenReturn(Flux.just(10L));

        when(iCapabilityPersistencePort.updateCapabilityTechnologiesStatusByCapabilityIds(
                capabilityIds, false))
                .thenReturn(Mono.empty());

        when(iCapabilityPersistencePort.updateCapabilitiesStatusByIds(
                capabilityIds, false))
                .thenReturn(Mono.empty());

        when(iTechnologyWebClientPort.deleteByIds(
                List.of(10L), token))
                .thenReturn(Mono.error(new RuntimeException("Error remoto")));

        when(iCapabilityPersistencePort.updateCapabilityTechnologiesStatusByCapabilityIds(
                capabilityIds, true))
                .thenReturn(Mono.error(new RuntimeException("Error rollback")));

        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(
                        capabilityDeleteUseCase.deleteByIds(capabilityIds, token)
                )
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.INTERNAL_ERROR
                )
                .verify();
    }

    @Test
    void shouldReturnRollbackErrorAfterExhaustingRetries() {
        List<Long> capabilityIds = List.of(1L);
        String token = "token";

        when(iCapabilityPersistencePort.findTechnologyIdsByCapabilityIds(capabilityIds))
                .thenReturn(Flux.just(10L));

        when(iCapabilityPersistencePort.updateCapabilityTechnologiesStatusByCapabilityIds(
                capabilityIds, false))
                .thenReturn(Mono.empty());

        when(iCapabilityPersistencePort.updateCapabilitiesStatusByIds(
                capabilityIds, false))
                .thenReturn(Mono.empty());

        when(iTechnologyWebClientPort.deleteByIds(List.of(10L), token))
                .thenReturn(Mono.error(new RuntimeException("Error remoto")));

        when(iCapabilityPersistencePort.updateCapabilityTechnologiesStatusByCapabilityIds(
                capabilityIds, true))
                .thenReturn(Mono.error(new RuntimeException("Error rollback")));

        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(
                        capabilityDeleteUseCase.deleteByIds(capabilityIds, token)
                )
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.INTERNAL_ERROR &&
                                error.getMessage().equals(
                                        DomainErrorMessages.ROLLBACK_ERROR
                                )
                )
                .verify();
    }
}
