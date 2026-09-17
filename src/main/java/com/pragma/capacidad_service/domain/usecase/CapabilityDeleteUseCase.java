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
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

import static reactor.netty.http.HttpConnectionLiveness.log;

@RequiredArgsConstructor
public class CapabilityDeleteUseCase implements ICapabilityDeleteServicePort {

    private static final int NUMBER_RETRY = 3;
    private static final int SECONDS_BEFORE_RETRY = 2;

    private final ICapabilityPersistencePort iCapabilityPersistencePort;
    private final ITechnologyWebClientPort iTechnologyWebClientPort;
    private final TransactionalOperator transactionalOperator;

    @Override
    public Mono<Void> deleteByIds(List<Long> capabilityIds, String token) {
        return Mono.defer(() -> {
            if (capabilityIds == null || capabilityIds.isEmpty()) {
                return Mono.error(new DomainException(
                        DomainErrorCode.VALIDATION_ERROR,
                        DomainErrorMessages.DELETE_IDS_REQUIRED
                ));
            }

            return iCapabilityPersistencePort.findTechnologyIdsByCapabilityIds(capabilityIds)
                    .collectList()
                    .flatMap(technologyIds -> updateCapabilityStatus(capabilityIds, false)
                            .then(Mono.defer(() -> callRemoteDeleteTechnology(capabilityIds, technologyIds, token))));

        }).onErrorMap(error -> {
            if (error instanceof DomainException) {
                return error;
            }

            return new DomainException(
                    DomainErrorCode.INTERNAL_ERROR,
                    DomainErrorMessages.CAPABILITY_DELETE_ROLLBACK_ERROR
            );
        });
    }


    private Mono<Void> callRemoteDeleteTechnology(List<Long> capabilityIds, List<Long> technologyIds, String token) {
        return iTechnologyWebClientPort.deleteByIds(technologyIds, token)
                .onErrorResume(throwable ->
                        updateCapabilityStatus(capabilityIds, true)
                                .retryWhen(Retry.fixedDelay(NUMBER_RETRY, Duration.ofSeconds(SECONDS_BEFORE_RETRY))
                                        .doBeforeRetry(retrySignal ->
                                                log.warn("Falló el rollback para las capcidades {}. Reintento #{} debido a: {}",
                                                        capabilityIds,
                                                        retrySignal.totalRetries(),
                                                        retrySignal.failure().getMessage()))
                                )
                                .onErrorResume(rollbackError -> {
                                    log.error("El rollback falló definitivamente tras agotar los reintentos para las capcidades {}", capabilityIds, rollbackError);
                                    return Mono.error(new DomainException(
                                            DomainErrorCode.INTERNAL_ERROR,
                                            DomainErrorMessages.ROLLBACK_ERROR
                                    ));
                                })
                                .then(Mono.error(new DomainException(
                                        DomainErrorCode.INTERNAL_ERROR,
                                        DomainErrorMessages.CAPABILITY_DELETE_ROLLBACK_ERROR
                                )))
                );
    }

    private Mono<Void> updateCapabilityStatus(List<Long> capabilityIds, Boolean status) {
        return iCapabilityPersistencePort.updateCapabilityTechnologiesStatusByCapabilityIds(capabilityIds, status)
                .then(Mono.defer(() -> iCapabilityPersistencePort.updateCapabilitiesStatusByIds(capabilityIds, status)))
                .as(transactionalOperator::transactional);
    }
}
