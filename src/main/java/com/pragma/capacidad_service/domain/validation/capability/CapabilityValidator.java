package com.pragma.capacidad_service.domain.validation.capability;

import com.pragma.capacidad_service.domain.exception.DomainErrorCode;
import com.pragma.capacidad_service.domain.exception.DomainErrorMessages;
import com.pragma.capacidad_service.domain.exception.DomainException;
import com.pragma.capacidad_service.domain.spi.ICapabilityPersistencePort;
import com.pragma.capacidad_service.domain.spi.ITechnologyWebClientPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;

@RequiredArgsConstructor
public class CapabilityValidator {

    private final ICapabilityPersistencePort iTechnologyPersistencePort;
    private final ITechnologyWebClientPort iTechnologyWebClientPort;

    public Mono<Void> validateCapability(
            String name,
            List<Long> technologyIds,
            String token
    ) {
        return validateTechnologyUniqueness(name)
                .then(Mono.defer(() -> validateTechnologies(technologyIds, token)));
    }

    private Mono<Void> validateTechnologyUniqueness(String name) {
        return iTechnologyPersistencePort.existsByName(name)
                .flatMap(technologyAlreadyExists ->
                        Boolean.TRUE.equals(technologyAlreadyExists)
                                ? Mono.error(new DomainException(
                                DomainErrorCode.DUPLICATE_NAME,
                                DomainErrorMessages.DUPLICATE_NAME
                        ))
                                : Mono.empty()
                );
    }

    private Mono<Void> validateTechnologies(
            List<Long> technologyIds,
            String token
    ) {
        return iTechnologyWebClientPort.existsByIds(technologyIds, token)
                .flatMap(existingTechnologyIds ->
                        existingTechnologyIds.size() == technologyIds.size()
                                && new HashSet<>(existingTechnologyIds).containsAll(technologyIds)
                                ? Mono.empty()
                                : Mono.error(new DomainException(
                                DomainErrorCode.TECHNOLOGY_NOT_FOUNT,
                                DomainErrorMessages.TECHNOLOGY_NOT_FOUND
                        ))
                );
    }

}
