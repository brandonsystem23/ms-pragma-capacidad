package com.pragma.capacidad_service.domain.validation.capability;


import com.pragma.capacidad_service.domain.exception.DomainErrorCode;
import com.pragma.capacidad_service.domain.exception.DomainErrorMessages;
import com.pragma.capacidad_service.domain.exception.DomainException;
import com.pragma.capacidad_service.domain.model.CapabilityCommand;
import com.pragma.capacidad_service.domain.validation.MaxTechnologiesValidator;
import com.pragma.capacidad_service.domain.validation.MinTechnologiesValidator;
import com.pragma.capacidad_service.domain.validation.ValidationUtils;

import java.util.HashSet;

public class DomainCapabilityValidator {

    public void validateUserCommand(CapabilityCommand command) {

        if (ValidationUtils.isBlank(command.name())) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.NAME_REQUIRED);
        }

        if (ValidationUtils.isBlank(command.description())) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.DESCRIPTION_REQUIRED);
        }

        if (command.technologyIds() == null || command.technologyIds().isEmpty()) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.TECHNOLOGIES_IDS_REQUIRED);
        }

        if (!MinTechnologiesValidator.isValid(command.technologyIds().size())) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.TECHNOLOGIES_MIN_LENGTH);
        }

        if (!MaxTechnologiesValidator.isValid(command.technologyIds().size())) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.TECHNOLOGIES_MAX_LENGTH);
        }

        if (new HashSet<>(command.technologyIds()).size() != command.technologyIds().size()) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.REPEATED_TECHNOLOGY);
        }

    }

}
