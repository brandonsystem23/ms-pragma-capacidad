package com.pragma.capacidad_service.infrastructure.out.mysql.repository;

import com.pragma.capacidad_service.infrastructure.out.mysql.entity.CapabilityTechnologyEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;


public interface ICapabilityTechnologyRepository extends ReactiveCrudRepository<CapabilityTechnologyEntity, Long> {

    Flux<CapabilityTechnologyEntity> findAllByCapabilityId(Long capabilityId);

}
