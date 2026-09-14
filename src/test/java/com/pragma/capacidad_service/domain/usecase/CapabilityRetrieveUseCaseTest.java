package com.pragma.capacidad_service.domain.usecase;

import com.pragma.capacidad_service.domain.model.Capability;
import com.pragma.capacidad_service.domain.model.PagedResult;
import com.pragma.capacidad_service.domain.model.Technology;
import com.pragma.capacidad_service.domain.model.command.CapabilityPageCommand;
import com.pragma.capacidad_service.domain.service.TechnologyDetailService;
import com.pragma.capacidad_service.domain.spi.ICapabilityPersistencePort;
import com.pragma.capacidad_service.domain.validation.capability.DomainCapabilityValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapabilityRetrieveUseCaseTest {

    @Mock
    private ICapabilityPersistencePort iCapabilityPersistencePort;

    @Mock
    private DomainCapabilityValidator domainCapabilityValidator;

    @Mock
    private TechnologyDetailService technologyDetailService;

    @InjectMocks
    private CapabilityRetrieveUseCase capabilityRetrieveUseCase;

    @Test
    void shouldGetCapabilitiesSuccessfully() {
        String token = "token";

        CapabilityPageCommand command = new CapabilityPageCommand(0, 10, "name", "asc");

        Capability capability = Capability.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .technologies(List.of(
                        Technology.builder().id(1L).build(),
                        Technology.builder().id(2L).build()
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

        List<Technology> enrichedTechnologies = List.of(
                Technology.builder().id(1L).name("Java").build(),
                Technology.builder().id(2L).name("Spring Boot").build()
        );

        doNothing().when(domainCapabilityValidator).validatePagination(command);

        when(iCapabilityPersistencePort.findAll(0, 10, "name", "asc"))
                .thenReturn(Mono.just(pagedResult));

        when(technologyDetailService.enrich(capability.getTechnologies(), token))
                .thenReturn(Mono.just(enrichedTechnologies));

        StepVerifier.create(capabilityRetrieveUseCase.getCapabilities(command, token))
                .assertNext(result -> {
                    Assertions.assertEquals(1, result.content().size());
                    Assertions.assertEquals("Backend", result.content().getFirst().getName());
                    Assertions.assertEquals(2, result.content().getFirst().getTechnologies().size());
                    Assertions.assertEquals("Java", result.content().getFirst().getTechnologies().get(0).getName());
                    Assertions.assertEquals("Spring Boot", result.content().getFirst().getTechnologies().get(1).getName());
                    Assertions.assertEquals(0, result.page());
                    Assertions.assertEquals(10, result.size());
                    Assertions.assertEquals(1, result.totalElements());
                    Assertions.assertEquals(1, result.totalPages());
                    Assertions.assertTrue(result.first());
                    Assertions.assertTrue(result.last());
                })
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenPaginationValidationFails() {
        String token = "token";
        CapabilityPageCommand command = new CapabilityPageCommand(-1, 10, "name", "asc");

        RuntimeException exception = new RuntimeException("error validando paginación");

        doThrow(exception)
                .when(domainCapabilityValidator)
                .validatePagination(command);

        StepVerifier.create(capabilityRetrieveUseCase.getCapabilities(command, token))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error validando paginación")
                )
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenFindAllFails() {
        String token = "token";
        CapabilityPageCommand command = new CapabilityPageCommand(0, 10, "name", "asc");

        doNothing().when(domainCapabilityValidator).validatePagination(command);

        when(iCapabilityPersistencePort.findAll(0, 10, "name", "asc"))
                .thenReturn(Mono.error(new RuntimeException("error consultando capacidades")));

        StepVerifier.create(capabilityRetrieveUseCase.getCapabilities(command, token))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error consultando capacidades")
                )
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenTechnologyEnrichmentFails() {
        String token = "token";

        CapabilityPageCommand command = new CapabilityPageCommand(0, 10, "name", "asc");

        Capability capability = Capability.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .technologies(List.of(
                        Technology.builder().id(1L).build()
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

        doNothing().when(domainCapabilityValidator).validatePagination(command);

        when(iCapabilityPersistencePort.findAll(0, 10, "name", "asc"))
                .thenReturn(Mono.just(pagedResult));

        when(technologyDetailService.enrich(capability.getTechnologies(), token))
                .thenReturn(Mono.error(new RuntimeException("error enriqueciendo tecnologías")));

        StepVerifier.create(capabilityRetrieveUseCase.getCapabilities(command, token))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error enriqueciendo tecnologías")
                )
                .verify();
    }

    @Test
    void shouldReturnEmptyPageSuccessfully() {
        String token = "token";
        CapabilityPageCommand command = new CapabilityPageCommand(0, 10, "name", "asc");

        PagedResult<Capability> pagedResult = PagedResult.<Capability>builder()
                .content(List.of())
                .page(0)
                .size(10)
                .totalElements(0)
                .totalPages(0)
                .first(true)
                .last(true)
                .build();

        doNothing().when(domainCapabilityValidator).validatePagination(command);

        when(iCapabilityPersistencePort.findAll(0, 10, "name", "asc"))
                .thenReturn(Mono.just(pagedResult));

        StepVerifier.create(capabilityRetrieveUseCase.getCapabilities(command, token))
                .assertNext(result -> {
                    Assertions.assertTrue(result.content().isEmpty());
                    Assertions.assertEquals(0, result.totalElements());
                    Assertions.assertEquals(0, result.totalPages());
                })
                .verifyComplete();
    }

    @Test
    void shouldRetrieveCapabilitiesByIdsSuccessfully() {
        String token = "token";
        List<Long> ids = List.of(1L, 2L);

        Capability capability1 = Capability.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .technologies(List.of(
                        Technology.builder().id(1L).build(),
                        Technology.builder().id(2L).build()
                ))
                .build();

        Capability capability2 = Capability.builder()
                .id(2L)
                .name("Frontend")
                .description("Capacidad frontend")
                .technologies(List.of(
                        Technology.builder().id(3L).build()
                ))
                .build();

        List<Technology> enrichedTechnologies1 = List.of(
                Technology.builder().id(1L).name("Java").build(),
                Technology.builder().id(2L).name("Spring Boot").build()
        );

        List<Technology> enrichedTechnologies2 = List.of(
                Technology.builder().id(3L).name("Angular").build()
        );

        when(iCapabilityPersistencePort.findByIds(ids))
                .thenReturn(reactor.core.publisher.Flux.just(
                        capability1,
                        capability2
                ));

        when(technologyDetailService.enrich(
                capability1.getTechnologies(),
                token
        )).thenReturn(Mono.just(enrichedTechnologies1));

        when(technologyDetailService.enrich(
                capability2.getTechnologies(),
                token
        )).thenReturn(Mono.just(enrichedTechnologies2));

        StepVerifier.create(
                        capabilityRetrieveUseCase.retrieveByIds(ids, token)
                )
                .assertNext(result -> {
                    Assertions.assertEquals(
                            1L,
                            result.getId()
                    );

                    Assertions.assertEquals(
                            "Backend",
                            result.getName()
                    );

                    Assertions.assertEquals(
                            2,
                            result.getTechnologies().size()
                    );

                    Assertions.assertEquals(
                            "Java",
                            result.getTechnologies().get(0).getName()
                    );

                    Assertions.assertEquals(
                            "Spring Boot",
                            result.getTechnologies().get(1).getName()
                    );
                })
                .assertNext(result -> {
                    Assertions.assertEquals(
                            2L,
                            result.getId()
                    );

                    Assertions.assertEquals(
                            "Frontend",
                            result.getName()
                    );

                    Assertions.assertEquals(
                            1,
                            result.getTechnologies().size()
                    );

                    Assertions.assertEquals(
                            "Angular",
                            result.getTechnologies().getFirst().getName()
                    );
                })
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenRetrieveByIdsFails() {
        String token = "token";
        List<Long> ids = List.of(1L, 2L);

        RuntimeException exception =
                new RuntimeException("error consultando capacidades por ids");

        when(iCapabilityPersistencePort.findByIds(ids))
                .thenReturn(
                        reactor.core.publisher.Flux.error(exception)
                );

        StepVerifier.create(
                        capabilityRetrieveUseCase.retrieveByIds(ids, token)
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals(
                                        "error consultando capacidades por ids"
                                )
                )
                .verify();
    }
}
