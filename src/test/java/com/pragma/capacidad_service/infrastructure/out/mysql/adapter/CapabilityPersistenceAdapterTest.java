package com.pragma.capacidad_service.infrastructure.out.mysql.adapter;

import com.pragma.capacidad_service.domain.model.Capability;
import com.pragma.capacidad_service.domain.model.Technology;
import com.pragma.capacidad_service.infrastructure.out.mysql.entity.CapabilityEntity;
import com.pragma.capacidad_service.infrastructure.out.mysql.entity.CapabilityTechnologyEntity;
import com.pragma.capacidad_service.infrastructure.out.mysql.mapper.CapabilityEntityMapper;
import com.pragma.capacidad_service.infrastructure.out.mysql.repository.ICapabilityRepository;
import com.pragma.capacidad_service.infrastructure.out.mysql.repository.ICapabilityTechnologyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapabilityPersistenceAdapterTest {

    @Mock
    private ICapabilityRepository iCapabilityRepository;

    @Mock
    private ICapabilityTechnologyRepository iCapabilityTechnologyRepository;

    @Mock
    private CapabilityEntityMapper capabilityEntityMapper;

    @InjectMocks
    private CapabilityPersistenceAdapter capabilityPersistenceAdapter;

    @Test
    void shouldSaveCapabilitySuccessfully() {

        Technology technology1 = Technology.builder()
                .id(1L)
                .build();

        Technology technology2 = Technology.builder()
                .id(2L)
                .build();

        Capability capability = Capability.builder()
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .status(true)
                .technologies(List.of(technology1, technology2))
                .build();

        CapabilityEntity entity = CapabilityEntity.builder()
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .status(true)
                .build();

        CapabilityEntity savedEntity = CapabilityEntity.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .status(true)
                .build();

        Capability savedCapability = Capability.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .status(true)
                .technologies(List.of(technology1, technology2))
                .build();

        CapabilityTechnologyEntity technologyEntity1 =
                CapabilityTechnologyEntity.builder()
                        .capabilityId(1L)
                        .technologyId(1L)
                        .status(true)
                        .build();

        CapabilityTechnologyEntity technologyEntity2 =
                CapabilityTechnologyEntity.builder()
                        .capabilityId(1L)
                        .technologyId(2L)
                        .status(true)
                        .build();

        when(capabilityEntityMapper.toEntity(capability))
                .thenReturn(entity);

        when(iCapabilityRepository.save(entity))
                .thenReturn(Mono.just(savedEntity));

        when(iCapabilityTechnologyRepository.saveAll(anyList()))
                .thenReturn(Flux.just(
                        technologyEntity1,
                        technologyEntity2
                ));

        when(capabilityEntityMapper.toTechnology(technologyEntity1))
                .thenReturn(technology1);

        when(capabilityEntityMapper.toTechnology(technologyEntity2))
                .thenReturn(technology2);

        when(capabilityEntityMapper.toDomain(savedEntity))
                .thenReturn(savedCapability);

        StepVerifier.create(
                        capabilityPersistenceAdapter.save(capability)
                )
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(
                            1L,
                            result.getId()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            "Desarrollo Backend",
                            result.getName()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            "Capacidad para desarrollar servicios backend",
                            result.getDescription()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            true,
                            result.getStatus()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            List.of(technology1, technology2),
                            result.getTechnologies()
                    );
                })
                .verifyComplete();

        verify(iCapabilityRepository).save(entity);
        verify(iCapabilityTechnologyRepository).saveAll(anyList());
    }

    @Test
    void shouldCheckIfCapabilityExistsByName() {

        String name = "Desarrollo Backend";

        when(iCapabilityRepository.existsByNameAndStatusTrue(name))
                .thenReturn(Mono.just(true));

        StepVerifier.create(
                        capabilityPersistenceAdapter.existsByName(name)
                )
                .expectNext(true)
                .verifyComplete();

    }

    @Test
    void shouldReturnFalseWhenCapabilityDoesNotExistByName() {

        String name = "Desarrollo Backend";

        when(iCapabilityRepository.existsByNameAndStatusTrue(name))
                .thenReturn(Mono.just(false));

        StepVerifier.create(
                        capabilityPersistenceAdapter.existsByName(name)
                )
                .expectNext(false)
                .verifyComplete();

    }

    @Test
    void shouldPropagateErrorWhenSavingCapability() {

        Technology technology = Technology.builder()
                .id(1L)
                .build();

        Capability capability = Capability.builder()
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .status(true)
                .technologies(List.of(technology))
                .build();

        CapabilityEntity entity = CapabilityEntity.builder()
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .status(true)
                .build();

        RuntimeException exception =
                new RuntimeException("Error guardando capacidad");

        when(capabilityEntityMapper.toEntity(capability))
                .thenReturn(entity);

        when(iCapabilityRepository.save(entity))
                .thenReturn(Mono.error(exception));

        StepVerifier.create(
                        capabilityPersistenceAdapter.save(capability)
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage()
                                        .equals("Error guardando capacidad")
                )
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenSavingCapabilityTechnologies() {

        Technology technology = Technology.builder()
                .id(1L)
                .build();

        Capability capability = Capability.builder()
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .status(true)
                .technologies(List.of(technology))
                .build();

        CapabilityEntity entity = CapabilityEntity.builder()
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .status(true)
                .build();

        CapabilityEntity savedEntity = CapabilityEntity.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .status(true)
                .build();

        RuntimeException exception =
                new RuntimeException("Error guardando tecnologías");

        when(capabilityEntityMapper.toEntity(capability))
                .thenReturn(entity);

        when(iCapabilityRepository.save(entity))
                .thenReturn(Mono.just(savedEntity));

        when(iCapabilityTechnologyRepository.saveAll(anyList()))
                .thenReturn(Flux.error(exception));

        StepVerifier.create(
                        capabilityPersistenceAdapter.save(capability)
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage()
                                        .equals("Error guardando tecnologías")
                )
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenCheckingCapabilityExistsByName() {

        String name = "Desarrollo Backend";

        RuntimeException exception =
                new RuntimeException("Error consultando capacidad");

        when(iCapabilityRepository.existsByNameAndStatusTrue(name))
                .thenReturn(Mono.error(exception));

        StepVerifier.create(
                        capabilityPersistenceAdapter.existsByName(name)
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage()
                                        .equals("Error consultando capacidad")
                )
                .verify();
    }

    @Test
    void shouldFindAllCapabilitiesPagedSuccessfullyOrderedByNameAsc() {
        CapabilityEntity capabilityEntity = CapabilityEntity.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .status(true)
                .build();

        Capability capability = Capability.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .status(true)
                .build();

        CapabilityTechnologyEntity relation1 = CapabilityTechnologyEntity.builder()
                .capabilityId(1L)
                .technologyId(1L)
                .status(true)
                .build();

        CapabilityTechnologyEntity relation2 = CapabilityTechnologyEntity.builder()
                .capabilityId(1L)
                .technologyId(2L)
                .status(true)
                .build();

        when(iCapabilityRepository.findAllOrderByNameAsc(10, 0))
                .thenReturn(Flux.just(capabilityEntity));

        when(iCapabilityTechnologyRepository.findAllByCapabilityId(1L))
                .thenReturn(Flux.just(relation1, relation2));

        when(capabilityEntityMapper.toDomain(capabilityEntity))
                .thenReturn(capability);

        when(iCapabilityRepository.countAllCapabilities())
                .thenReturn(Mono.just(1L));

        StepVerifier.create(capabilityPersistenceAdapter.findAll(0, 10, "name", "asc"))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(1, result.content().size());
                    org.junit.jupiter.api.Assertions.assertEquals("Backend", result.content().getFirst().getName());
                    org.junit.jupiter.api.Assertions.assertEquals(2, result.content().getFirst().getTechnologies().size());
                    org.junit.jupiter.api.Assertions.assertEquals(true, result.content().getFirst().getStatus());
                    org.junit.jupiter.api.Assertions.assertEquals(0, result.page());
                    org.junit.jupiter.api.Assertions.assertEquals(10, result.size());
                    org.junit.jupiter.api.Assertions.assertEquals(1L, result.totalElements());
                    org.junit.jupiter.api.Assertions.assertEquals(1, result.totalPages());
                    org.junit.jupiter.api.Assertions.assertTrue(result.first());
                    org.junit.jupiter.api.Assertions.assertTrue(result.last());
                })
                .verifyComplete();
    }

    @Test
    void shouldFindAllCapabilitiesPagedSuccessfullyOrderedByTechnologyCountDesc() {
        CapabilityEntity capabilityEntity = CapabilityEntity.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .status(true)
                .build();

        Capability capability = Capability.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .status(true)
                .build();

        CapabilityTechnologyEntity relation = CapabilityTechnologyEntity.builder()
                .capabilityId(1L)
                .technologyId(1L)
                .status(true)
                .build();

        when(iCapabilityRepository.findAllOrderByTechnologyCountDesc(5, 5))
                .thenReturn(Flux.just(capabilityEntity));

        when(iCapabilityTechnologyRepository.findAllByCapabilityId(1L))
                .thenReturn(Flux.just(relation));

        when(capabilityEntityMapper.toDomain(capabilityEntity))
                .thenReturn(capability);

        when(iCapabilityRepository.countAllCapabilities())
                .thenReturn(Mono.just(6L));

        StepVerifier.create(capabilityPersistenceAdapter.findAll(1, 5, "numberTechnologies", "desc"))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(1, result.content().size());
                    org.junit.jupiter.api.Assertions.assertEquals(1, result.page());
                    org.junit.jupiter.api.Assertions.assertEquals(5, result.size());
                    org.junit.jupiter.api.Assertions.assertEquals(6L, result.totalElements());
                    org.junit.jupiter.api.Assertions.assertEquals(2, result.totalPages());
                    org.junit.jupiter.api.Assertions.assertFalse(result.first());
                    org.junit.jupiter.api.Assertions.assertTrue(result.last());
                    org.junit.jupiter.api.Assertions.assertEquals(1, result.content().getFirst().getTechnologies().size());
                })
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenFindAllCapabilitiesFails() {

        when(iCapabilityRepository.findAllOrderByNameAsc(10, 0))
                .thenReturn(Flux.error(
                        new RuntimeException("error consultando capacidades")
                ));

        when(iCapabilityRepository.countAllCapabilities())
                .thenReturn(Mono.just(0L));

        StepVerifier.create(
                        capabilityPersistenceAdapter.findAll(0, 10, "name", "asc")
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error consultando capacidades")
                )
                .verify();
    }

    @Test
    void shouldFindAllCapabilitiesPagedSuccessfullyOrderedByNameDesc() {

        CapabilityEntity capabilityEntity = CapabilityEntity.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .status(true)
                .build();

        Capability capability = Capability.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .status(true)
                .build();

        CapabilityTechnologyEntity relation = CapabilityTechnologyEntity.builder()
                .capabilityId(1L)
                .technologyId(1L)
                .status(true)
                .build();

        when(iCapabilityRepository.findAllOrderByNameDesc(10, 0))
                .thenReturn(Flux.just(capabilityEntity));

        when(iCapabilityTechnologyRepository.findAllByCapabilityId(1L))
                .thenReturn(Flux.just(relation));

        when(capabilityEntityMapper.toDomain(capabilityEntity))
                .thenReturn(capability);

        when(iCapabilityRepository.countAllCapabilities())
                .thenReturn(Mono.just(1L));

        StepVerifier.create(
                        capabilityPersistenceAdapter.findAll(
                                0,
                                10,
                                "name",
                                "desc"
                        )
                )
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(
                            1,
                            result.content().size()
                    );
                    org.junit.jupiter.api.Assertions.assertEquals(
                            "Backend",
                            result.content().getFirst().getName()
                    );
                    org.junit.jupiter.api.Assertions.assertEquals(
                            true,
                            result.content().getFirst().getStatus()
                    );
                    org.junit.jupiter.api.Assertions.assertEquals(
                            0,
                            result.page()
                    );
                    org.junit.jupiter.api.Assertions.assertEquals(
                            10,
                            result.size()
                    );
                    org.junit.jupiter.api.Assertions.assertEquals(
                            1L,
                            result.totalElements()
                    );
                    org.junit.jupiter.api.Assertions.assertEquals(
                            1,
                            result.totalPages()
                    );
                    org.junit.jupiter.api.Assertions.assertTrue(
                            result.first()
                    );
                    org.junit.jupiter.api.Assertions.assertTrue(
                            result.last()
                    );
                    org.junit.jupiter.api.Assertions.assertEquals(
                            1,
                            result.content()
                                    .getFirst()
                                    .getTechnologies()
                                    .size()
                    );
                })
                .verifyComplete();
    }

    @Test
    void shouldFindAllCapabilitiesPagedSuccessfullyOrderedByTechnologyCountAsc() {

        CapabilityEntity capabilityEntity = CapabilityEntity.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .status(true)
                .build();

        Capability capability = Capability.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .status(true)
                .build();

        CapabilityTechnologyEntity relation = CapabilityTechnologyEntity.builder()
                .capabilityId(1L)
                .technologyId(1L)
                .status(true)
                .build();

        when(iCapabilityRepository.findAllOrderByTechnologyCountAsc(5, 5))
                .thenReturn(Flux.just(capabilityEntity));

        when(iCapabilityTechnologyRepository.findAllByCapabilityId(1L))
                .thenReturn(Flux.just(relation));

        when(capabilityEntityMapper.toDomain(capabilityEntity))
                .thenReturn(capability);

        when(iCapabilityRepository.countAllCapabilities())
                .thenReturn(Mono.just(6L));

        StepVerifier.create(
                        capabilityPersistenceAdapter.findAll(
                                1,
                                5,
                                "numberTechnologies",
                                "asc"
                        )
                )
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(
                            1,
                            result.content().size()
                    );
                    org.junit.jupiter.api.Assertions.assertEquals(
                            "Backend",
                            result.content().getFirst().getName()
                    );
                    org.junit.jupiter.api.Assertions.assertEquals(
                            1,
                            result.page()
                    );
                    org.junit.jupiter.api.Assertions.assertEquals(
                            5,
                            result.size()
                    );
                    org.junit.jupiter.api.Assertions.assertEquals(
                            6L,
                            result.totalElements()
                    );
                    org.junit.jupiter.api.Assertions.assertEquals(
                            2,
                            result.totalPages()
                    );
                    org.junit.jupiter.api.Assertions.assertFalse(
                            result.first()
                    );
                    org.junit.jupiter.api.Assertions.assertTrue(
                            result.last()
                    );
                    org.junit.jupiter.api.Assertions.assertEquals(
                            1,
                            result.content()
                                    .getFirst()
                                    .getTechnologies()
                                    .size()
                    );
                })
                .verifyComplete();
    }

    @Test
    void shouldFindAllCapabilitiesUsingDefaultOrderWhenSortByIsUnknown() {

        CapabilityEntity capabilityEntity = CapabilityEntity.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .status(true)
                .build();

        Capability capability = Capability.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .status(true)
                .build();

        CapabilityTechnologyEntity relation = CapabilityTechnologyEntity.builder()
                .capabilityId(1L)
                .technologyId(1L)
                .status(true)
                .build();

        when(iCapabilityRepository.findAllOrderByNameAsc(10, 0))
                .thenReturn(Flux.just(capabilityEntity));

        when(iCapabilityTechnologyRepository.findAllByCapabilityId(1L))
                .thenReturn(Flux.just(relation));

        when(capabilityEntityMapper.toDomain(capabilityEntity))
                .thenReturn(capability);

        when(iCapabilityRepository.countAllCapabilities())
                .thenReturn(Mono.just(1L));

        StepVerifier.create(
                        capabilityPersistenceAdapter.findAll(
                                0,
                                10,
                                "unknown",
                                "asc"
                        )
                )
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(
                            1,
                            result.content().size()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            "Backend",
                            result.content().getFirst().getName()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            true,
                            result.content().getFirst().getStatus()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            0,
                            result.page()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            10,
                            result.size()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            1L,
                            result.totalElements()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            1,
                            result.totalPages()
                    );

                    org.junit.jupiter.api.Assertions.assertTrue(
                            result.first()
                    );

                    org.junit.jupiter.api.Assertions.assertTrue(
                            result.last()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            1,
                            result.content()
                                    .getFirst()
                                    .getTechnologies()
                                    .size()
                    );
                })
                .verifyComplete();

        verify(iCapabilityRepository)
                .findAllOrderByNameAsc(10, 0);
    }

    @Test
    void shouldReturnFalseForLastPageWhenThereAreMorePages() {

        CapabilityEntity capabilityEntity = CapabilityEntity.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .status(true)
                .build();

        Capability capability = Capability.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .status(true)
                .build();

        CapabilityTechnologyEntity relation = CapabilityTechnologyEntity.builder()
                .capabilityId(1L)
                .technologyId(1L)
                .status(true)
                .build();

        when(iCapabilityRepository.findAllOrderByNameAsc(5, 0))
                .thenReturn(Flux.just(capabilityEntity));

        when(iCapabilityTechnologyRepository.findAllByCapabilityId(1L))
                .thenReturn(Flux.just(relation));

        when(capabilityEntityMapper.toDomain(capabilityEntity))
                .thenReturn(capability);

        when(iCapabilityRepository.countAllCapabilities())
                .thenReturn(Mono.just(11L));

        StepVerifier.create(
                        capabilityPersistenceAdapter.findAll(
                                0,
                                5,
                                "name",
                                "asc"
                        )
                )
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(
                            1,
                            result.content().size()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            0,
                            result.page()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            5,
                            result.size()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            11L,
                            result.totalElements()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            3,
                            result.totalPages()
                    );

                    org.junit.jupiter.api.Assertions.assertTrue(
                            result.first()
                    );

                    org.junit.jupiter.api.Assertions.assertFalse(
                            result.last()
                    );
                })
                .verifyComplete();

        verify(iCapabilityRepository)
                .findAllOrderByNameAsc(5, 0);

        verify(iCapabilityRepository)
                .countAllCapabilities();
    }

    @Test
    void shouldFindExistingTechnologyIdsSuccessfully() {
        List<Long> ids = List.of(1L, 2L, 3L);
        List<Long> existingIds = List.of(1L, 3L);

        when(iCapabilityRepository.findExistingIds(ids))
                .thenReturn(Flux.fromIterable(existingIds));

        StepVerifier.create(capabilityPersistenceAdapter.findExistingIds(ids))
                .expectNext(1L)
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void shouldFindCapabilitiesByIdsSuccessfully() {

        List<Long> ids = List.of(1L, 2L);

        CapabilityEntity capabilityEntity1 = CapabilityEntity.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .status(true)
                .build();

        CapabilityEntity capabilityEntity2 = CapabilityEntity.builder()
                .id(2L)
                .name("Frontend")
                .description("Capacidad frontend")
                .status(true)
                .build();

        Capability capability1 = Capability.builder()
                .id(1L)
                .name("Backend")
                .description("Capacidad backend")
                .status(true)
                .build();

        Capability capability2 = Capability.builder()
                .id(2L)
                .name("Frontend")
                .description("Capacidad frontend")
                .status(true)
                .build();

        CapabilityTechnologyEntity relation1 = CapabilityTechnologyEntity.builder()
                .capabilityId(1L)
                .technologyId(10L)
                .status(true)
                .build();

        CapabilityTechnologyEntity relation2 = CapabilityTechnologyEntity.builder()
                .capabilityId(1L)
                .technologyId(20L)
                .status(true)
                .build();

        CapabilityTechnologyEntity relation3 = CapabilityTechnologyEntity.builder()
                .capabilityId(2L)
                .technologyId(30L)
                .status(true)
                .build();

        when(iCapabilityRepository.findByIdIn(ids))
                .thenReturn(Flux.just(capabilityEntity1, capabilityEntity2));

        when(iCapabilityTechnologyRepository.findAllByCapabilityId(1L))
                .thenReturn(Flux.just(relation1, relation2));

        when(iCapabilityTechnologyRepository.findAllByCapabilityId(2L))
                .thenReturn(Flux.just(relation3));

        when(capabilityEntityMapper.toDomain(capabilityEntity1))
                .thenReturn(capability1);

        when(capabilityEntityMapper.toDomain(capabilityEntity2))
                .thenReturn(capability2);

        StepVerifier.create(
                        capabilityPersistenceAdapter.findByIds(ids)
                )
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(
                            1L,
                            result.getId()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            "Backend",
                            result.getName()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            true,
                            result.getStatus()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            2,
                            result.getTechnologies().size()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            10L,
                            result.getTechnologies().get(0).getId()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            20L,
                            result.getTechnologies().get(1).getId()
                    );
                })
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(
                            2L,
                            result.getId()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            "Frontend",
                            result.getName()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            true,
                            result.getStatus()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            1,
                            result.getTechnologies().size()
                    );

                    org.junit.jupiter.api.Assertions.assertEquals(
                            30L,
                            result.getTechnologies().getFirst().getId()
                    );
                })
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenFindByIdsFails() {

        List<Long> ids = List.of(1L, 2L);

        RuntimeException exception =
                new RuntimeException("Error consultando capacidades por ids");

        when(iCapabilityRepository.findByIdIn(ids))
                .thenReturn(Flux.error(exception));

        StepVerifier.create(
                        capabilityPersistenceAdapter.findByIds(ids)
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals(
                                        "Error consultando capacidades por ids"
                                )
                )
                .verify();
    }

    @Test
    void shouldFindTechnologyIdsByCapabilityIdsSuccessfully() {
        List<Long> ids = List.of(1L, 2L);

        when(iCapabilityTechnologyRepository.findTechnologyIdsByCapabilityIds(ids))
                .thenReturn(Flux.just(10L, 20L, 30L));

        StepVerifier.create(capabilityPersistenceAdapter.findTechnologyIdsByCapabilityIds(ids))
                .expectNext(10L)
                .expectNext(20L)
                .expectNext(30L)
                .verifyComplete();
    }

    @Test
    void shouldUpdateCapabilityTechnologiesStatusByCapabilityIdsSuccessfully() {
        List<Long> ids = List.of(1L, 2L);

        when(iCapabilityTechnologyRepository.updateStatusByCapabilityIds(ids, false))
                .thenReturn(Mono.just(2));

        StepVerifier.create(
                        capabilityPersistenceAdapter.updateCapabilityTechnologiesStatusByCapabilityIds(ids, false)
                )
                .verifyComplete();
    }

    @Test
    void shouldUpdateCapabilitiesStatusByIdsSuccessfully() {
        List<Long> ids = List.of(1L, 2L);

        when(iCapabilityRepository.updateStatusByIds(ids, false))
                .thenReturn(Mono.just(2));

        StepVerifier.create(
                        capabilityPersistenceAdapter.updateCapabilitiesStatusByIds(ids, false)
                )
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenFindingTechnologyIdsByCapabilityIdsFails() {
        List<Long> ids = List.of(1L, 2L);

        when(iCapabilityTechnologyRepository.findTechnologyIdsByCapabilityIds(ids))
                .thenReturn(Flux.error(new RuntimeException("error consultando ids de tecnologías")));

        StepVerifier.create(capabilityPersistenceAdapter.findTechnologyIdsByCapabilityIds(ids))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error consultando ids de tecnologías"))
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenUpdatingCapabilityTechnologiesStatusFails() {
        List<Long> ids = List.of(1L, 2L);

        when(iCapabilityTechnologyRepository.updateStatusByCapabilityIds(ids, true))
                .thenReturn(Mono.error(new RuntimeException("error actualizando relaciones")));

        StepVerifier.create(
                        capabilityPersistenceAdapter.updateCapabilityTechnologiesStatusByCapabilityIds(ids, true)
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error actualizando relaciones"))
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenUpdatingCapabilitiesStatusFails() {
        List<Long> ids = List.of(1L, 2L);

        when(iCapabilityRepository.updateStatusByIds(ids, true))
                .thenReturn(Mono.error(new RuntimeException("error actualizando capacidades")));

        StepVerifier.create(
                        capabilityPersistenceAdapter.updateCapabilitiesStatusByIds(ids, true)
                )
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("error actualizando capacidades"))
                .verify();
    }

}
