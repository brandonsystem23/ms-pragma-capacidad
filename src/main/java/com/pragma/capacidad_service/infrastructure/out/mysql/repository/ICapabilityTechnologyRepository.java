package com.pragma.capacidad_service.infrastructure.out.mysql.repository;

import com.pragma.capacidad_service.infrastructure.out.mysql.entity.CapabilityTechnologyEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICapabilityTechnologyRepository extends ReactiveCrudRepository<CapabilityTechnologyEntity, Long> {

    @Query("""
        SELECT id, capability_id, technology_id, status
        FROM capability_technology
        WHERE capability_id = :capabilityId
          AND status = true
        """)
    Flux<CapabilityTechnologyEntity> findAllByCapabilityId(Long capabilityId);

    @Query("""
        SELECT DISTINCT technology_id
        FROM capability_technology
        WHERE capability_id IN (:capabilityIds)
          AND status = true
        """)
    Flux<Long> findTechnologyIdsByCapabilityIds(List<Long> capabilityIds);

    @Modifying
    @Query("""
        UPDATE capability_technology
        SET status = :status
        WHERE capability_id IN (:capabilityIds)
        """)
    Mono<Integer> updateStatusByCapabilityIds(List<Long> capabilityIds, Boolean status);
}
