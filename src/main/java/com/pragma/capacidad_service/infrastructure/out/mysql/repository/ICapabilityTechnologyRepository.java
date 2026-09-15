package com.pragma.capacidad_service.infrastructure.out.mysql.repository;

import com.pragma.capacidad_service.infrastructure.out.mysql.entity.CapabilityTechnologyEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICapabilityTechnologyRepository extends ReactiveCrudRepository<CapabilityTechnologyEntity, Long> {

    Flux<CapabilityTechnologyEntity> findAllByCapabilityId(Long capabilityId);

    @Query("""
        SELECT technology_id
        FROM capability_technology
        WHERE capability_id IN (:capabilityIds)
        """)
    Flux<Long> findTechnologyIdsByCapabilityIds(List<Long> capabilityIds);

    @Modifying
    @Query("""
        DELETE FROM capability_technology
        WHERE capability_id IN (:capabilityIds)
        """)
    Mono<Integer> deleteByCapabilityIds(List<Long> capabilityIds);
}
