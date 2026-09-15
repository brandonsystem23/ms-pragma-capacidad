package com.pragma.capacidad_service.domain.usecase;

import com.pragma.capacidad_service.domain.exception.DomainErrorCode;
import com.pragma.capacidad_service.domain.exception.DomainErrorMessages;
import com.pragma.capacidad_service.domain.exception.DomainException;
import com.pragma.capacidad_service.domain.model.Capability;
import com.pragma.capacidad_service.domain.model.Technology;
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
    void shouldDeleteCapabilitiesAndTechnologiesSuccessfully() {
        List<Long> capabilityIds = List.of(1L, 2L);
        String token = "token";

        Capability capability1 = Capability.builder()
                .id(1L)
                .technologies(List.of(
                        Technology.builder().id(10L).build(),
                        Technology.builder().id(20L).build()
                ))
                .build();

        Capability capability2 = Capability.builder()
                .id(2L)
                .technologies(List.of(
                        Technology.builder().id(30L).build()
                ))
                .build();

        when(iCapabilityPersistencePort.findByIds(capabilityIds))
                .thenReturn(Flux.just(capability1, capability2));
        when(iCapabilityPersistencePort.deleteCapabilityTechnologiesByCapabilityIds(capabilityIds))
                .thenReturn(Mono.empty());
        when(iCapabilityPersistencePort.deleteCapabilitiesByIds(capabilityIds))
                .thenReturn(Mono.empty());
        when(iTechnologyWebClientPort.deleteByIds(List.of(10L, 20L, 30L), token))
                .thenReturn(Mono.empty());
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(capabilityDeleteUseCase.deleteByIds(capabilityIds, token))
                .verifyComplete();
    }

    @Test
    void shouldDeleteCapabilitiesWithoutCallingWebClientWhenNoTechnologiesExist() {
        List<Long> capabilityIds = List.of(1L);
        String token = "token";

        Capability capability = Capability.builder()
                .id(1L)
                .technologies(List.of())
                .build();

        when(iCapabilityPersistencePort.findByIds(capabilityIds))
                .thenReturn(Flux.just(capability));
        when(iCapabilityPersistencePort.deleteCapabilityTechnologiesByCapabilityIds(capabilityIds))
                .thenReturn(Mono.empty());
        when(iCapabilityPersistencePort.deleteCapabilitiesByIds(capabilityIds))
                .thenReturn(Mono.empty());
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(capabilityDeleteUseCase.deleteByIds(capabilityIds, token))
                .verifyComplete();
    }

    @Test
    void shouldRollbackAndReturnDomainExceptionWhenDeleteRelationsFails() {
        List<Long> capabilityIds = List.of(1L);
        String token = "token";

        Capability capability = Capability.builder()
                .id(1L)
                .technologies(List.of(Technology.builder().id(10L).build()))
                .build();

        when(iCapabilityPersistencePort.findByIds(capabilityIds))
                .thenReturn(Flux.just(capability));
        when(iCapabilityPersistencePort.deleteCapabilityTechnologiesByCapabilityIds(capabilityIds))
                .thenReturn(Mono.error(new RuntimeException("error eliminando relaciones")));
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
    void shouldRollbackAndReturnDomainExceptionWhenDeleteCapabilitiesFails() {
        List<Long> capabilityIds = List.of(1L);
        String token = "token";

        Capability capability = Capability.builder()
                .id(1L)
                .technologies(List.of(Technology.builder().id(10L).build()))
                .build();

        when(iCapabilityPersistencePort.findByIds(capabilityIds))
                .thenReturn(Flux.just(capability));
        when(iCapabilityPersistencePort.deleteCapabilityTechnologiesByCapabilityIds(capabilityIds))
                .thenReturn(Mono.empty());
        when(iCapabilityPersistencePort.deleteCapabilitiesByIds(capabilityIds))
                .thenReturn(Mono.error(new RuntimeException("error eliminando capacidades")));
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
    void shouldRollbackAndReturnDomainExceptionWhenTechnologyDeleteFails() {
        List<Long> capabilityIds = List.of(1L);
        String token = "token";

        Capability capability = Capability.builder()
                .id(1L)
                .technologies(List.of(Technology.builder().id(10L).build()))
                .build();

        when(iCapabilityPersistencePort.findByIds(capabilityIds))
                .thenReturn(Flux.just(capability));
        when(iCapabilityPersistencePort.deleteCapabilityTechnologiesByCapabilityIds(capabilityIds))
                .thenReturn(Mono.empty());
        when(iCapabilityPersistencePort.deleteCapabilitiesByIds(capabilityIds))
                .thenReturn(Mono.empty());
        when(iTechnologyWebClientPort.deleteByIds(List.of(10L), token))
                .thenReturn(Mono.error(new RuntimeException("rollback remoto")));
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
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(capabilityDeleteUseCase.deleteByIds(List.of(), "token"))
                .expectErrorMatches(error ->
                        error instanceof DomainException &&
                                ((DomainException) error).getCode() == DomainErrorCode.VALIDATION_ERROR &&
                                error.getMessage().equals(DomainErrorMessages.DELETE_IDS_REQUIRED))
                .verify();
    }
}
