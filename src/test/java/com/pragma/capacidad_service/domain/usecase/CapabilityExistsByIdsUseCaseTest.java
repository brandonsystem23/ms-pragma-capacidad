package com.pragma.capacidad_service.domain.usecase;

import com.pragma.capacidad_service.domain.spi.ICapabilityPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapabilityExistsByIdsUseCaseTest {

    @Mock
    private ICapabilityPersistencePort iCapabilityPersistencePort;

    @InjectMocks
    private CapabilityExistsByIdsUseCase capabilityExistsByIdsUseCase;

    @Test
    void shouldRetrieveExistingIdsSuccessfully() {
        List<Long> ids = List.of(1L, 2L, 3L);
        List<Long> existingIds = List.of(1L, 3L);

        when(iCapabilityPersistencePort.findExistingIds(ids))
                .thenReturn(Flux.fromIterable(existingIds));

        StepVerifier.create(capabilityExistsByIdsUseCase.retrieveExistingIds(ids))
                .expectNext(1L)
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenRetrieveExistingIdsFails() {
        List<Long> ids = List.of(1L, 2L, 3L);

        when(iCapabilityPersistencePort.findExistingIds(ids))
                .thenReturn(Flux.error(
                        new RuntimeException("error consultando IDs de tecnologías")
                ));

        StepVerifier.create(capabilityExistsByIdsUseCase.retrieveExistingIds(ids))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error consultando IDs de tecnologías"))
                .verify();
    }
}