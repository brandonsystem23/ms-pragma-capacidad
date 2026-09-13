package com.pragma.capacidad_service.infrastructure.configuration;

import com.pragma.capacidad_service.domain.api.ICapabilityExistsByIdsServicePort;
import com.pragma.capacidad_service.domain.api.ICapabilityRegisterServicePort;
import com.pragma.capacidad_service.domain.api.ICapabilityRetrieveServicePort;
import com.pragma.capacidad_service.domain.service.TechnologyDetailService;
import com.pragma.capacidad_service.domain.spi.ICapabilityPersistencePort;
import com.pragma.capacidad_service.domain.spi.ITechnologyWebClientPort;
import com.pragma.capacidad_service.domain.usecase.CapabilityExistsByIdsUseCase;
import com.pragma.capacidad_service.domain.usecase.CapabilityRegisterUseCase;
import com.pragma.capacidad_service.domain.usecase.CapabilityRetrieveUseCase;
import com.pragma.capacidad_service.domain.validation.capability.DomainCapabilityValidator;
import com.pragma.capacidad_service.domain.validation.capability.CapabilityValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {


    @Bean
    public DomainCapabilityValidator domainCapabilityValidator() {
        return new DomainCapabilityValidator();
    }


    @Bean
    public CapabilityValidator capabilityValidator(ICapabilityPersistencePort iCapabilityPersistencePort,
                                                   ITechnologyWebClientPort iTechnologyWebClientPort) {
        return new CapabilityValidator(iCapabilityPersistencePort, iTechnologyWebClientPort);
    }

    @Bean
    public ICapabilityRegisterServicePort capabilityRegisterUseCase(
            ICapabilityPersistencePort iCapabilityPersistencePort,
            DomainCapabilityValidator domainCapabilityValidator,
            CapabilityValidator capabilityValidator

    ) {
        return new CapabilityRegisterUseCase(
                iCapabilityPersistencePort,
                domainCapabilityValidator,
                capabilityValidator
        );
    }

    @Bean
    public ICapabilityRetrieveServicePort capabilityQueryUseCase(
            ICapabilityPersistencePort iCapabilityPersistencePort,
            DomainCapabilityValidator domainCapabilityValidator,
            TechnologyDetailService technologyDetailService
    ) {
        return new CapabilityRetrieveUseCase(
                iCapabilityPersistencePort,
                domainCapabilityValidator,
                technologyDetailService
        );
    }

    @Bean
    public ICapabilityExistsByIdsServicePort capabilityExistsByIdsUseCase(
            ICapabilityPersistencePort iCapabilityPersistencePort
    ) {
        return new CapabilityExistsByIdsUseCase(
                iCapabilityPersistencePort
        );
    }

    @Bean
    public TechnologyDetailService technologyDetailService(
            ITechnologyWebClientPort iTechnologyWebClientPort
    ) {
        return new TechnologyDetailService(iTechnologyWebClientPort);
    }

}
