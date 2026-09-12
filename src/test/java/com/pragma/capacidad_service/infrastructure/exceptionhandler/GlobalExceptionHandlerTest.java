package com.pragma.capacidad_service.infrastructure.exceptionhandler;

import com.pragma.capacidad_service.domain.exception.DomainErrorCode;
import com.pragma.capacidad_service.domain.exception.DomainException;
import com.pragma.capacidad_service.infrastructure.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;
    private ServerWebExchange serverWebExchange;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();

        serverWebExchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/capability/create").build()
        );
    }

    @Test
    void shouldHandleValidationDomainException() {

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleDomainException(
                        new DomainException(
                                DomainErrorCode.VALIDATION_ERROR,
                                "El campo name es obligatorio"
                        ),
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
        assertEquals("Bad Request", response.error());
        assertEquals("El campo name es obligatorio", response.message());
        assertEquals("/api/v1/capability/create", response.path());
        assertNotNull(response.timestamp());
        assertTrue(response.details().isEmpty());
    }

    @Test
    void shouldHandleTechnologyNotFoundDomainException() {

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleDomainException(
                        new DomainException(
                                DomainErrorCode.TECHNOLOGY_NOT_FOUNT,
                                "Una o más tecnologías no existen"
                        ),
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
        assertEquals("Bad Request", response.error());
        assertEquals("Una o más tecnologías no existen", response.message());
    }

    @Test
    void shouldHandleDuplicateNameDomainException() {

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleDomainException(
                        new DomainException(
                                DomainErrorCode.DUPLICATE_NAME,
                                "El nombre de la capacidad ya está registrado"
                        ),
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
        assertEquals("Bad Request", response.error());
        assertEquals(
                "El nombre de la capacidad ya está registrado",
                response.message()
        );
    }

    @Test
    void shouldHandleInvalidTokenDomainException() {

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleDomainException(
                        new DomainException(
                                DomainErrorCode.INVALID_TOKEN,
                                "Token inválido o expirado"
                        ),
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.status());
        assertEquals("Unauthorized", response.error());
        assertEquals("Token inválido o expirado", response.message());
    }

    @Test
    void shouldHandleAccessDeniedDomainException() {

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleDomainException(
                        new DomainException(
                                DomainErrorCode.ACCESS_DENIED,
                                "No tienes permisos para acceder a este recurso"
                        ),
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.status());
        assertEquals("Forbidden", response.error());
        assertEquals(
                "No tienes permisos para acceder a este recurso",
                response.message()
        );
    }

    @Test
    void shouldHandleExternalServiceDomainException() {

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleDomainException(
                        new DomainException(
                                DomainErrorCode.EXTERNAL_SERVICE_ERROR,
                                "Error externo"
                        ),
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(HttpStatus.BAD_GATEWAY, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_GATEWAY.value(), response.status());
        assertEquals("Bad Gateway", response.error());
        assertEquals("Error externo", response.message());
    }

    @Test
    void shouldHandleInternalErrorDomainException() {

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleDomainException(
                        new DomainException(
                                DomainErrorCode.INTERNAL_ERROR,
                                "Ocurrió un error interno en el servidor"
                        ),
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                responseEntity.getStatusCode()
        );
        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                response.status()
        );
        assertEquals("Internal Server Error", response.error());
        assertEquals(
                "Ocurrió un error interno en el servidor",
                response.message()
        );
    }

    @Test
    void shouldHandleExternalServiceException() {

        ExternalServiceException exception = new ExternalServiceException(
                HttpStatus.NOT_FOUND,
                "Tecnologías no encontradas"
        );

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleExternalServiceException(
                        exception,
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.status());
        assertEquals("Not Found", response.error());
        assertEquals("Tecnologías no encontradas", response.message());
        assertEquals("/api/v1/capability/create", response.path());
        assertNotNull(response.timestamp());
        assertTrue(response.details().isEmpty());
    }

    @Test
    void shouldHandleIllegalArgument() {

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleIllegalArgument(
                        new IllegalArgumentException("Parámetro inválido"),
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
        assertEquals("Bad Request", response.error());
        assertEquals("Parámetro inválido", response.message());
        assertEquals("/api/v1/capability/create", response.path());
        assertNotNull(response.timestamp());
        assertTrue(response.details().isEmpty());
    }

    @Test
    void shouldHandleGenericException() {

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleGeneric(
                        new RuntimeException("Error inesperado"),
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                responseEntity.getStatusCode()
        );
        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                response.status()
        );
        assertEquals("Internal Server Error", response.error());
        assertEquals(
                "Ocurrió un error interno en el servidor",
                response.message()
        );
        assertEquals("/api/v1/capability/create", response.path());
        assertNotNull(response.timestamp());
        assertTrue(response.details().isEmpty());
    }

    @Test
    void shouldHandleValidationErrors() {

        WebExchangeBindException exception =
                mock(WebExchangeBindException.class);

        FieldError nameRequiredError = new FieldError(
                "capabilityRequest",
                "name",
                "El campo name es obligatorio"
        );

        FieldError descriptionRequiredError = new FieldError(
                "capabilityRequest",
                "description",
                "El campo description es obligatorio"
        );

        when(exception.getFieldErrors())
                .thenReturn(List.of(
                        nameRequiredError,
                        descriptionRequiredError
                ));

        ResponseEntity<ErrorResponse> responseEntity =
                globalExceptionHandler.handleValidationErrors(
                        exception,
                        serverWebExchange
                );

        ErrorResponse response = getBody(responseEntity);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
        assertEquals("Bad Request", response.error());
        assertEquals("Error de validación", response.message());
        assertEquals("/api/v1/capability/create", response.path());
        assertNotNull(response.timestamp());

        assertEquals(
                List.of(
                        "name: El campo name es obligatorio",
                        "description: El campo description es obligatorio"
                ),
                response.details()
        );
    }

    private ErrorResponse getBody(
            ResponseEntity<ErrorResponse> responseEntity
    ) {
        ErrorResponse body = responseEntity.getBody();
        assertNotNull(body);
        return body;
    }
}