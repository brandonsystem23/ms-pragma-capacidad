package com.pragma.capacidad_service.domain.usecase;

import com.pragma.capacidad_service.domain.model.Capability;
import com.pragma.capacidad_service.domain.model.PagedResult;
import com.pragma.capacidad_service.domain.model.Technology;
import com.pragma.capacidad_service.domain.model.command.CapabilityPageCommand;
import com.pragma.capacidad_service.domain.service.TechnologyDetailService;
import com.pragma.capacidad_service.domain.spi.ICapabilityPersistencePort;
import com.pragma.capacidad_service.domain.validation.capability.DomainCapabilityValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.doNothing;
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
                    org.junit.jupiter.api.Assertions.assertEquals(1, result.content().size());
                    org.junit.jupiter.api.Assertions.assertEquals("Backend", result.content().get(0).getName());
                    org.junit.jupiter.api.Assertions.assertEquals(2, result.content().get(0).getTechnologies().size());
                    org.junit.jupiter.api.Assertions.assertEquals("Java", result.content().get(0).getTechnologies().get(0).getName());
                    org.junit.jupiter.api.Assertions.assertEquals("Spring Boot", result.content().get(0).getTechnologies().get(1).getName());
                    org.junit.jupiter.api.Assertions.assertEquals(0, result.page());
                    org.junit.jupiter.api.Assertions.assertEquals(10, result.size());
                    org.junit.jupiter.api.Assertions.assertEquals(1, result.totalElements());
                    org.junit.jupiter.api.Assertions.assertEquals(1, result.totalPages());
                    org.junit.jupiter.api.Assertions.assertTrue(result.first());
                    org.junit.jupiter.api.Assertions.assertTrue(result.last());
                })
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenPaginationValidationFails() {
        String token = "token";
        CapabilityPageCommand command = new CapabilityPageCommand(-1, 10, "name", "asc");

        RuntimeException exception = new RuntimeException("error validando paginación");

        org.mockito.Mockito.doThrow(exception)
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
                    org.junit.jupiter.api.Assertions.assertTrue(result.content().isEmpty());
                    org.junit.jupiter.api.Assertions.assertEquals(0, result.totalElements());
                    org.junit.jupiter.api.Assertions.assertEquals(0, result.totalPages());
                })
                .verifyComplete();
    }
}
