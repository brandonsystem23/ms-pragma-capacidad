package com.pragma.capacidad_service.domain.usecase;

import com.pragma.capacidad_service.domain.api.ICapabilityDeleteServicePort;
import com.pragma.capacidad_service.domain.exception.DomainErrorCode;
import com.pragma.capacidad_service.domain.exception.DomainErrorMessages;
import com.pragma.capacidad_service.domain.exception.DomainException;
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

            return iCapabilityPersistencePort.findTechnologyIdsByCapabilityIds(ids)
                    .collectList()
                    .flatMap(technologyIds ->
                            executeLocalSoftDelete(ids)
                                    .then(Mono.defer(() -> callRemoteDeleteTechnology(ids, technologyIds, token)))
                    );
        }).onErrorMap(throwable -> {
            if (throwable instanceof DomainException) {
                return throwable;
            }

            return new DomainException(
                    DomainErrorCode.INTERNAL_ERROR,
                    DomainErrorMessages.CAPABILITY_DELETE_ROLLBACK_ERROR
            );
        });
    }

    private Mono<Void> executeLocalSoftDelete(List<Long> capabilityIds) {
        return iCapabilityPersistencePort.updateCapabilityTechnologiesStatusByCapabilityIds(capabilityIds, false)
                .then(iCapabilityPersistencePort.updateCapabilitiesStatusByIds(capabilityIds, false))
                .as(transactionalOperator::transactional);
    }

    private Mono<Void> callRemoteDeleteTechnology(List<Long> capabilityIds, List<Long> technologyIds, String token) {

        if (technologyIds.isEmpty()) {
            return Mono.empty();
        }

        return iTechnologyWebClientPort.deleteByIds(technologyIds, token)
                .onErrorResume(throwable ->
                        rollbackStatuses(capabilityIds)
                                .then(Mono.error(new DomainException(
                                        DomainErrorCode.INTERNAL_ERROR,
                                        DomainErrorMessages.CAPABILITY_DELETE_ROLLBACK_ERROR
                                )))
                );
    }

    private Mono<Void> rollbackStatuses(List<Long> capabilityIds) {
        return iCapabilityPersistencePort.updateCapabilityTechnologiesStatusByCapabilityIds(capabilityIds, true)
                .then(iCapabilityPersistencePort.updateCapabilitiesStatusByIds(capabilityIds, true))
                .as(transactionalOperator::transactional);
    }
}
