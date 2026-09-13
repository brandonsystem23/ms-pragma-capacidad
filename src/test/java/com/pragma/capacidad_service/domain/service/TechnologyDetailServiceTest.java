package com.pragma.capacidad_service.domain.service;

import com.pragma.capacidad_service.domain.model.Technology;
import com.pragma.capacidad_service.domain.spi.ITechnologyWebClientPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnologyDetailServiceTest {

    @Mock
    private ITechnologyWebClientPort iTechnologyWebClientPort;

    @InjectMocks
    private TechnologyDetailService technologyDetailService;

    @Test
    void shouldEnrichTechnologiesSuccessfully() {

        Technology technology1 = Technology.builder()
                .id(1L)
                .build();

        Technology technology2 = Technology.builder()
                .id(2L)
                .build();

        List<Technology> technologies = List.of(
                technology1,
                technology2
        );

        String token = "Bearer token";

        when(iTechnologyWebClientPort.findByIds(
                List.of(1L, 2L),
                token
        )).thenReturn(Mono.just(technologies));

        StepVerifier.create(
                        technologyDetailService.enrich(technologies, token)
                )
                .assertNext(result -> {
                    assertEquals(2, result.size());
                    assertEquals(1L, result.get(0).getId());
                    assertEquals(2L, result.get(1).getId());
                })
                .verifyComplete();

        verify(iTechnologyWebClientPort).findByIds(
                List.of(1L, 2L),
                token
        );

        verifyNoMoreInteractions(iTechnologyWebClientPort);
    }

    @Test
    void shouldRemoveDuplicatedTechnologyIdsBeforeCallingWebClient() {

        Technology technology1 = Technology.builder()
                .id(1L)
                .build();

        Technology technology1Duplicated = Technology.builder()
                .id(1L)
                .build();

        Technology technology2 = Technology.builder()
                .id(2L)
                .build();

        List<Technology> technologies = List.of(
                technology1,
                technology1Duplicated,
                technology2
        );

        String token = "Bearer token";

        List<Technology> enrichedTechnologies = List.of(
                technology1,
                technology2
        );

        when(iTechnologyWebClientPort.findByIds(
                List.of(1L, 2L),
                token
        )).thenReturn(Mono.just(enrichedTechnologies));

        StepVerifier.create(
                        technologyDetailService.enrich(technologies, token)
                )
                .assertNext(result -> {
                    assertEquals(2, result.size());
                    assertEquals(1L, result.get(0).getId());
                    assertEquals(2L, result.get(1).getId());
                })
                .verifyComplete();

        verify(iTechnologyWebClientPort).findByIds(
                List.of(1L, 2L),
                token
        );

        verifyNoMoreInteractions(iTechnologyWebClientPort);
    }

    @Test
    void shouldReturnEmptyListWhenNoTechnologiesAreProvided() {

        List<Technology> technologies = List.of();
        String token = "Bearer token";

        when(iTechnologyWebClientPort.findByIds(
                List.of(),
                token
        )).thenReturn(Mono.just(List.of()));

        StepVerifier.create(
                        technologyDetailService.enrich(technologies, token)
                )
                .assertNext(result -> assertEquals(0, result.size()))
                .verifyComplete();

        verify(iTechnologyWebClientPort).findByIds(
                List.of(),
                token
        );

        verifyNoMoreInteractions(iTechnologyWebClientPort);
    }

    @Test
    void shouldPropagateErrorWhenFindingTechnologies() {

        Technology technology = Technology.builder()
                .id(1L)
                .build();

        List<Technology> technologies = List.of(technology);
        String token = "Bearer token";

        RuntimeException exception =
                new RuntimeException("Error consultando tecnologías");

        when(iTechnologyWebClientPort.findByIds(
                List.of(1L),
                token
        )).thenReturn(Mono.error(exception));

        StepVerifier.create(
                        technologyDetailService.enrich(technologies, token)
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals(
                                        "Error consultando tecnologías"
                                )
                )
                .verify();

        verify(iTechnologyWebClientPort).findByIds(
                List.of(1L),
                token
        );

        verifyNoMoreInteractions(iTechnologyWebClientPort);
    }
}

