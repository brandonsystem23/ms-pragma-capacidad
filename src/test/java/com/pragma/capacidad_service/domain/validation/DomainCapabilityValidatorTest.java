package com.pragma.capacidad_service.domain.validation;

import com.pragma.capacidad_service.domain.exception.DomainErrorCode;
import com.pragma.capacidad_service.domain.exception.DomainErrorMessages;
import com.pragma.capacidad_service.domain.exception.DomainException;
import com.pragma.capacidad_service.domain.model.CapabilityCommand;
import com.pragma.capacidad_service.domain.validation.capability.DomainCapabilityValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class DomainCapabilityValidatorTest {

    private DomainCapabilityValidator domainCapabilityValidator;

    @BeforeEach
    void setUp() {
        domainCapabilityValidator = new DomainCapabilityValidator();
    }

    @Test
    void shouldPassWhenCommandIsValid() {

        CapabilityCommand command = new CapabilityCommand(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                List.of(1L, 2L, 3L)
        );

        Assertions.assertDoesNotThrow(() ->
                domainCapabilityValidator.validateUserCommand(command)
        );
    }

    @Test
    void shouldFailWhenNameIsNull() {

        CapabilityCommand command = new CapabilityCommand(
                null,
                "Capacidad para desarrollar servicios backend",
                List.of(1L, 2L, 3L)
        );

        DomainException exception = Assertions.assertThrows(
                DomainException.class,
                () -> domainCapabilityValidator.validateUserCommand(command)
        );

        Assertions.assertEquals(
                DomainErrorCode.VALIDATION_ERROR,
                exception.getCode()
        );

        Assertions.assertEquals(
                DomainErrorMessages.NAME_REQUIRED,
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenNameIsBlank() {

        CapabilityCommand command = new CapabilityCommand(
                "   ",
                "Capacidad para desarrollar servicios backend",
                List.of(1L, 2L, 3L)
        );

        DomainException exception = Assertions.assertThrows(
                DomainException.class,
                () -> domainCapabilityValidator.validateUserCommand(command)
        );

        Assertions.assertEquals(
                DomainErrorCode.VALIDATION_ERROR,
                exception.getCode()
        );

        Assertions.assertEquals(
                DomainErrorMessages.NAME_REQUIRED,
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenDescriptionIsNull() {

        CapabilityCommand command = new CapabilityCommand(
                "Desarrollo Backend",
                null,
                List.of(1L, 2L, 3L)
        );

        DomainException exception = Assertions.assertThrows(
                DomainException.class,
                () -> domainCapabilityValidator.validateUserCommand(command)
        );

        Assertions.assertEquals(
                DomainErrorCode.VALIDATION_ERROR,
                exception.getCode()
        );

        Assertions.assertEquals(
                DomainErrorMessages.DESCRIPTION_REQUIRED,
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenDescriptionIsBlank() {

        CapabilityCommand command = new CapabilityCommand(
                "Desarrollo Backend",
                "   ",
                List.of(1L, 2L, 3L)
        );

        DomainException exception = Assertions.assertThrows(
                DomainException.class,
                () -> domainCapabilityValidator.validateUserCommand(command)
        );

        Assertions.assertEquals(
                DomainErrorCode.VALIDATION_ERROR,
                exception.getCode()
        );

        Assertions.assertEquals(
                DomainErrorMessages.DESCRIPTION_REQUIRED,
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenTechnologyIdsAreNull() {

        CapabilityCommand command = new CapabilityCommand(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                null
        );

        DomainException exception = Assertions.assertThrows(
                DomainException.class,
                () -> domainCapabilityValidator.validateUserCommand(command)
        );

        Assertions.assertEquals(
                DomainErrorCode.VALIDATION_ERROR,
                exception.getCode()
        );

        Assertions.assertEquals(
                DomainErrorMessages.TECHNOLOGIES_IDS_REQUIRED,
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenTechnologyIdsAreEmpty() {

        CapabilityCommand command = new CapabilityCommand(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                List.of()
        );

        DomainException exception = Assertions.assertThrows(
                DomainException.class,
                () -> domainCapabilityValidator.validateUserCommand(command)
        );

        Assertions.assertEquals(
                DomainErrorCode.VALIDATION_ERROR,
                exception.getCode()
        );

        Assertions.assertEquals(
                DomainErrorMessages.TECHNOLOGIES_IDS_REQUIRED,
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenTechnologyIdsAreBelowMinimum() {

        CapabilityCommand command = new CapabilityCommand(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                List.of(1L)
        );

        DomainException exception = Assertions.assertThrows(
                DomainException.class,
                () -> domainCapabilityValidator.validateUserCommand(command)
        );

        Assertions.assertEquals(
                DomainErrorCode.VALIDATION_ERROR,
                exception.getCode()
        );

        Assertions.assertEquals(
                DomainErrorMessages.TECHNOLOGIES_MIN_LENGTH,
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenTechnologyIdsExceedMaximum() {

        List<Long> technologyIds = List.of(
                1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L,17L, 18L, 19L, 20L, 21L
        );

        CapabilityCommand command = new CapabilityCommand(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                technologyIds
        );

        DomainException exception = Assertions.assertThrows(
                DomainException.class,
                () -> domainCapabilityValidator.validateUserCommand(command)
        );

        Assertions.assertEquals(
                DomainErrorCode.VALIDATION_ERROR,
                exception.getCode()
        );

        Assertions.assertEquals(
                DomainErrorMessages.TECHNOLOGIES_MAX_LENGTH,
                exception.getMessage()
        );
    }

    @Test
    void shouldFailWhenTechnologyIdsAreRepeated() {

        CapabilityCommand command = new CapabilityCommand(
                "Desarrollo Backend",
                "Capacidad para desarrollar servicios backend",
                List.of(1L, 2L, 2L)
        );

        DomainException exception = Assertions.assertThrows(
                DomainException.class,
                () -> domainCapabilityValidator.validateUserCommand(command)
        );

        Assertions.assertEquals(
                DomainErrorCode.VALIDATION_ERROR,
                exception.getCode()
        );

        Assertions.assertEquals(
                DomainErrorMessages.REPEATED_TECHNOLOGY,
                exception.getMessage()
        );
    }
}