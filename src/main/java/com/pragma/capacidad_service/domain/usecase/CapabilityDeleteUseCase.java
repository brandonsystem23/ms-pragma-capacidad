package com.pragma.capacidad_service.domain.usecase;

import com.pragma.capacidad_service.domain.api.ICapabilityDeleteServicePort;
import com.pragma.capacidad_service.domain.exception.DomainErrorCode;
import com.pragma.capacidad_service.domain.exception.DomainErrorMessages;
import com.pragma.capacidad_service.domain.exception.DomainException;
import com.pragma.capacidad_service.domain.model.Capability;
import com.pragma.capacidad_service.domain.model.Technology;
import com.pragma.capacidad_service.domain.spi.ICapabilityPersistencePort;
import com.pragma.capacidad_service.domain.spi.ITechnologyWebClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class CapabilityDeleteUseCase implements ICapabilityDeleteServicePort {

    private final ICapabilityPersistencePort iCapabilityPersistencePort;
    private final ITechnologyWebClientPort iTechnologyWebClientPort;
    private final TransactionalOperator transactionalOperator;

    @Override
    public Mono<Void> deleteByIds(List<Long> ids, String token) {
        return Mono.defer(() -> {
                    if (ids == null || ids.isEmpty()) {
                        return Mono.error(new DomainException(
                                DomainErrorCode.VALIDATION_ERROR,
                                DomainErrorMessages.DELETE_IDS_REQUIRED
                        ));
                    }

                    return iCapabilityPersistencePort.findByIds(ids)
                            .collectList()
                            .flatMap(capabilities -> executeDelete(ids, capabilities, token));
                })
                .as(transactionalOperator::transactional)
                .onErrorMap(throwable -> {
                    if (throwable instanceof DomainException) {
                        return throwable;
                    }

                    return new DomainException(
                            DomainErrorCode.INTERNAL_ERROR,
                            DomainErrorMessages.CAPABILITY_DELETE_ROLLBACK_ERROR
                    );
                });
    }

    private Mono<Void> executeDelete(List<Long> capabilityIds, List<Capability> capabilities, String token) {
        List<Long> technologyIds = capabilities.stream()
                .flatMap(capability -> capability.getTechnologies().stream())
                .map(Technology::getId)
                .distinct()
                .toList();

        return iCapabilityPersistencePort.deleteCapabilityTechnologiesByCapabilityIds(capabilityIds)
                .then(Mono.defer(() -> iCapabilityPersistencePort.deleteCapabilitiesByIds(capabilityIds)))
                .then(Mono.defer(() -> {
                    if (technologyIds.isEmpty()) {
                        return Mono.empty();
                    }
                    return iTechnologyWebClientPort.deleteByIds(technologyIds, token);
                }));
    }
}
