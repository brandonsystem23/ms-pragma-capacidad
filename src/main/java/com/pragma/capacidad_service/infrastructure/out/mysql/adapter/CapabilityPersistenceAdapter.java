package com.pragma.capacidad_service.infrastructure.out.mysql.adapter;

import com.pragma.capacidad_service.domain.model.Capability;
import com.pragma.capacidad_service.domain.model.PagedResult;
import com.pragma.capacidad_service.domain.model.Technology;
import com.pragma.capacidad_service.domain.spi.ICapabilityPersistencePort;
import com.pragma.capacidad_service.infrastructure.out.mysql.entity.CapabilityEntity;
import com.pragma.capacidad_service.infrastructure.out.mysql.entity.CapabilityTechnologyEntity;
import com.pragma.capacidad_service.infrastructure.out.mysql.mapper.CapabilityEntityMapper;
import com.pragma.capacidad_service.infrastructure.out.mysql.repository.ICapabilityRepository;
import com.pragma.capacidad_service.infrastructure.out.mysql.repository.ICapabilityTechnologyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CapabilityPersistenceAdapter implements ICapabilityPersistencePort {

    private final ICapabilityRepository iCapabilityRepository;
    private final ICapabilityTechnologyRepository iCapabilityTechnologyRepository;
    private final CapabilityEntityMapper capabilityEntityMapper;

    @Override
    public Mono<Capability> save(Capability capability) {
        CapabilityEntity capabilityEntity = capabilityEntityMapper.toEntity(capability);
        return iCapabilityRepository.save(capabilityEntity)
                .flatMap(savedCapabilityEntity -> saveItems(savedCapabilityEntity.getId(), capability.getTechnologies())
                        .map(savedItems -> buildOrder(savedCapabilityEntity, savedItems)));
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return iCapabilityRepository.existsByName(name);
    }

    @Override
    public Mono<PagedResult<Capability>> findAll(int page, int size, String sortBy, String direction) {

        long offset = (long) page * size;

        Flux<CapabilityEntity> capabilities = switch (sortBy.toLowerCase()) {
            case "name" -> "desc".equalsIgnoreCase(direction)
                    ? iCapabilityRepository.findAllOrderByNameDesc(size, offset)
                    : iCapabilityRepository.findAllOrderByNameAsc(size, offset);

            case "numbertechnologies" -> "desc".equalsIgnoreCase(direction)
                    ? iCapabilityRepository.findAllOrderByTechnologyCountDesc(size, offset)
                    : iCapabilityRepository.findAllOrderByTechnologyCountAsc(size, offset);

            default -> iCapabilityRepository.findAllOrderByNameAsc(size, offset);
        };

        Mono<List<Capability>> capabilitiesWithTechnologies = capabilities
                .concatMap(capabilityEntity ->
                        findTechnologyIdsByCapabilityId(capabilityEntity.getId())
                                .map(technologyIds -> {
                                    Capability capability =
                                            capabilityEntityMapper.toDomain(capabilityEntity);

                                    List<Technology> technologies = technologyIds.stream()
                                            .map(technologyId -> Technology.builder()
                                                    .id(technologyId)
                                                    .build())
                                            .toList();

                                    capability.setTechnologies(technologies);

                                    return capability;
                                })
                )
                .collectList();

        return Mono.zip(
                capabilitiesWithTechnologies,
                iCapabilityRepository.countAllCapabilities()
        ).map(tuple -> {
            List<Capability> content = tuple.getT1();
            long totalElements = tuple.getT2();
            int totalPages = (int) Math.ceil((double) totalElements / size);

            return PagedResult.<Capability>builder()
                    .content(content)
                    .page(page)
                    .size(size)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .first(page == 0)
                    .last(page >= totalPages - 1)
                    .build();
        });
    }

    private Mono<List<Long>> findTechnologyIdsByCapabilityId(Long capabilityId) {
        return iCapabilityTechnologyRepository.findAllByCapabilityId(capabilityId)
                .map(CapabilityTechnologyEntity::getTechnologyId)
                .collectList();
    }

    private Mono<List<Technology>> saveItems(Long capabilityId, List<Technology> items) {

        List<CapabilityTechnologyEntity> entities = items.stream()
                .map(item -> CapabilityTechnologyEntity.builder()
                        .capabilityId(capabilityId)
                        .technologyId(item.getId())
                        .build()
                )
                .toList();

        return iCapabilityTechnologyRepository.saveAll(entities)
                .map(capabilityEntityMapper::toTechnology)
                .collectList();
    }

    private Capability buildOrder(CapabilityEntity capabilityEntity, List<Technology> items) {
        Capability capability = capabilityEntityMapper.toDomain(capabilityEntity);
        capability.setTechnologies(items);
        return capability;
    }
}