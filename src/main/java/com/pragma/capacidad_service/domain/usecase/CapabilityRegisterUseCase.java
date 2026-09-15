package com.pragma.capacidad_service.domain.usecase;

import com.pragma.capacidad_service.domain.api.ICapabilityRegisterServicePort;
import com.pragma.capacidad_service.domain.builder.CapabilityBuilder;
import com.pragma.capacidad_service.domain.exception.DomainErrorCode;
import com.pragma.capacidad_service.domain.exception.DomainErrorMessages;
import com.pragma.capacidad_service.domain.exception.DomainException;
import com.pragma.capacidad_service.domain.model.Capability;
import com.pragma.capacidad_service.domain.model.command.CapabilityCommand;
import com.pragma.capacidad_service.domain.spi.ICapabilityPersistencePort;
import com.pragma.capacidad_service.domain.validation.capability.DomainCapabilityValidator;
import com.pragma.capacidad_service.domain.validation.capability.CapabilityValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CapabilityRegisterUseCase implements ICapabilityRegisterServicePort {

    private final ICapabilityPersistencePort iCapabilityPersistencePort;
    private final DomainCapabilityValidator domainCapabilityValidator;
    private final CapabilityValidator capabilityValidator;
    private final TransactionalOperator transactionalOperator;


    @Override
    public Mono<Capability> create(CapabilityCommand capabilityCommand, String token) {
        return Mono.defer(() -> {
            domainCapabilityValidator.validateUserCommand(capabilityCommand);

            return capabilityValidator.validateCapability(capabilityCommand.name(),
                            capabilityCommand.technologyIds(), token)
                    .then(Mono.defer(() -> {
                        Capability capability = CapabilityBuilder.buildCapability(capabilityCommand);
                        return iCapabilityPersistencePort.save(capability);
                    }));
        }).as(transactionalOperator::transactional)
                .onErrorMap(throwable -> {
                    if (throwable instanceof DomainException) {
                        return throwable;
                    }

                    return new DomainException(
                            DomainErrorCode.INTERNAL_ERROR,
                            DomainErrorMessages.CAPABILITY_SAVE_ROLLBACK_ERROR
                    );
                });
    }
}
