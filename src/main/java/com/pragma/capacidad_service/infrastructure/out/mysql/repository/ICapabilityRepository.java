package com.pragma.capacidad_service.infrastructure.out.mysql.repository;

import com.pragma.capacidad_service.infrastructure.out.mysql.entity.CapabilityEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICapabilityRepository extends ReactiveCrudRepository<CapabilityEntity, Long> {

    Mono<Boolean> existsByName(String name);

    @Query("""
            SELECT c.id, c.name, c.description, COUNT(ct.technology_id) AS number_technologies
            FROM capability c
            LEFT JOIN capability_technology ct ON c.id = ct.capability_id
            GROUP BY c.id, c.name, c.description
            ORDER BY c.name ASC
            LIMIT :size OFFSET :offset
            """)
    Flux<CapabilityEntity> findAllOrderByNameAsc(int size, long offset);

    @Query("""
            SELECT c.id, c.name, c.description, COUNT(ct.technology_id) AS number_technologies
            FROM capability c
            LEFT JOIN capability_technology ct ON c.id = ct.capability_id
            GROUP BY c.id, c.name, c.description
            ORDER BY c.name DESC
            LIMIT :size OFFSET :offset
            """)
    Flux<CapabilityEntity> findAllOrderByNameDesc(int size, long offset);

    @Query("""
            SELECT c.id, c.name, c.description, COUNT(ct.technology_id) AS number_technologies
            FROM capability c
            LEFT JOIN capability_technology ct ON c.id = ct.capability_id
            GROUP BY c.id, c.name, c.description
            ORDER BY COUNT(ct.technology_id) ASC, c.name ASC
            LIMIT :size OFFSET :offset
            """)
    Flux<CapabilityEntity> findAllOrderByTechnologyCountAsc(int size, long offset);

    @Query("""
            SELECT c.id, c.name, c.description, COUNT(ct.technology_id) AS number_technologies
            FROM capability c
            LEFT JOIN capability_technology ct ON c.id = ct.capability_id
            GROUP BY c.id, c.name, c.description
            ORDER BY COUNT(ct.technology_id) DESC, c.name ASC
            LIMIT :size OFFSET :offset
            """)
    Flux<CapabilityEntity> findAllOrderByTechnologyCountDesc(int size, long offset);

    @Query("SELECT COUNT(*) FROM capability")
    Mono<Long> countAllCapabilities();

    @Query("""
        SELECT id
        FROM capability
        WHERE id IN (:ids)
        """)
    Flux<Long> findExistingIds(List<Long> ids);

    Flux<CapabilityEntity> findByIdIn(List<Long> ids);
}
