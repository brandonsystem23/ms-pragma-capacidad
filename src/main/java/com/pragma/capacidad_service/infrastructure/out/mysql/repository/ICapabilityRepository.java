package com.pragma.capacidad_service.infrastructure.out.mysql.repository;

import com.pragma.capacidad_service.infrastructure.out.mysql.entity.CapabilityEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICapabilityRepository extends ReactiveCrudRepository<CapabilityEntity, Long> {

    Mono<Boolean> existsByNameAndStatusTrue(String name);

    @Query("""
            SELECT c.id, c.name, c.description, c.status
            FROM capability c
            LEFT JOIN capability_technology ct
                ON c.id = ct.capability_id AND ct.status = true
            WHERE c.status = true
            GROUP BY c.id, c.name, c.description, c.status
            ORDER BY c.name ASC
            LIMIT :size OFFSET :offset
            """)
    Flux<CapabilityEntity> findAllOrderByNameAsc(int size, long offset);

    @Query("""
            SELECT c.id, c.name, c.description, c.status
            FROM capability c
            LEFT JOIN capability_technology ct
                ON c.id = ct.capability_id AND ct.status = true
            WHERE c.status = true
            GROUP BY c.id, c.name, c.description, c.status
            ORDER BY c.name DESC
            LIMIT :size OFFSET :offset
            """)
    Flux<CapabilityEntity> findAllOrderByNameDesc(int size, long offset);

    @Query("""
            SELECT c.id, c.name, c.description, c.status
            FROM capability c
            LEFT JOIN capability_technology ct
                ON c.id = ct.capability_id AND ct.status = true
            WHERE c.status = true
            GROUP BY c.id, c.name, c.description, c.status
            ORDER BY COUNT(ct.technology_id) ASC, c.name ASC
            LIMIT :size OFFSET :offset
            """)
    Flux<CapabilityEntity> findAllOrderByTechnologyCountAsc(int size, long offset);

    @Query("""
            SELECT c.id, c.name, c.description, c.status
            FROM capability c
            LEFT JOIN capability_technology ct
                ON c.id = ct.capability_id AND ct.status = true
            WHERE c.status = true
            GROUP BY c.id, c.name, c.description, c.status
            ORDER BY COUNT(ct.technology_id) DESC, c.name ASC
            LIMIT :size OFFSET :offset
            """)
    Flux<CapabilityEntity> findAllOrderByTechnologyCountDesc(int size, long offset);

    @Query("""
            SELECT COUNT(*)
            FROM capability
            WHERE status = true
            """)
    Mono<Long> countAllCapabilities();

    @Query("""
        SELECT id
        FROM capability
        WHERE id IN (:ids)
          AND status = true
        """)
    Flux<Long> findExistingIds(List<Long> ids);

    @Query("""
        SELECT id, name, description, status
        FROM capability
        WHERE id IN (:ids)
          AND status = true
        """)
    Flux<CapabilityEntity> findByIdIn(List<Long> ids);

    @Modifying
    @Query("""
    UPDATE capability
    SET status = :status
    WHERE id IN (:ids)
    """)
    Mono<Integer> updateStatusByIds(List<Long> ids, Boolean status);

}
