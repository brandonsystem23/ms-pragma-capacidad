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
                .technologies(List.of(technology1, technology2))
                .build();

        CapabilityEntity entity = CapabilityEntity.builder()
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .build();

        CapabilityEntity savedEntity = CapabilityEntity.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .build();

        Capability savedCapability = Capability.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .technologies(List.of(technology1, technology2))
                .build();

        CapabilityTechnologyEntity technologyEntity1 =
                CapabilityTechnologyEntity.builder()
                        .capabilityId(1L)
                        .technologyId(1L)
                        .build();

        CapabilityTechnologyEntity technologyEntity2 =
                CapabilityTechnologyEntity.builder()
                        .capabilityId(1L)
                        .technologyId(2L)
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

        when(iCapabilityRepository.existsByName(name))
                .thenReturn(Mono.just(true));

        StepVerifier.create(
                        capabilityPersistenceAdapter.existsByName(name)
                )
                .expectNext(true)
                .verifyComplete();

        verify(iCapabilityRepository).existsByName(name);
    }

    @Test
    void shouldReturnFalseWhenCapabilityDoesNotExistByName() {

        String name = "Desarrollo Backend";

        when(iCapabilityRepository.existsByName(name))
                .thenReturn(Mono.just(false));

        StepVerifier.create(
                        capabilityPersistenceAdapter.existsByName(name)
                )
                .expectNext(false)
                .verifyComplete();

        verify(iCapabilityRepository).existsByName(name);
    }

    @Test
    void shouldPropagateErrorWhenSavingCapability() {

        Technology technology = Technology.builder()
                .id(1L)
                .build();

        Capability capability = Capability.builder()
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .technologies(List.of(technology))
                .build();

        CapabilityEntity entity = CapabilityEntity.builder()
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
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
                .technologies(List.of(technology))
                .build();

        CapabilityEntity entity = CapabilityEntity.builder()
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
                .build();

        CapabilityEntity savedEntity = CapabilityEntity.builder()
                .id(1L)
                .name("Desarrollo Backend")
                .description("Capacidad para desarrollar servicios backend")
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

        when(iCapabilityRepository.existsByName(name))
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
}