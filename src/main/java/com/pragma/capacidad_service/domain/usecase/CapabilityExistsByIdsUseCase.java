package com.pragma.capacidad_service.domain.usecase;

import com.pragma.capacidad_service.domain.api.ICapabilityExistsByIdsServicePort;
import com.pragma.capacidad_service.domain.spi.ICapabilityPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.List;

@RequiredArgsConstructor
public class CapabilityExistsByIdsUseCase implements ICapabilityExistsByIdsServicePort {

    private final ICapabilityPersistencePort iCapabilityPersistencePort;

    @Override
    public Flux<Long> retrieveExistingIds(List<Long> ids) {
        return iCapabilityPersistencePort.findExistingIds(ids);
    }
}
