package com.pragma.capacidad_service.infrastructure.input.rest;

import com.pragma.capacidad_service.application.dto.request.CapabilityRequest;
import com.pragma.capacidad_service.application.dto.response.CapabilityResponse;
import com.pragma.capacidad_service.application.handler.ICapabilityHandler;
import com.pragma.capacidad_service.infrastructure.util.UtilTokenExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/capability")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Capacidad", description = "Endpoint para gestion de capacidades")
public class CapabilityController {

    private final ICapabilityHandler iCapabilityHandler;

    @PostMapping("/create")
    @Operation(summary = "Crear capacidad", description = "Crear una capacidad. Requiere rol ADMINISTRADOR")
    public Mono<CapabilityResponse> createTechnology(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody CapabilityRequest request
    ) {

        log.info("Solicitud para crear una tecnologia");

        String token = UtilTokenExtractor.extract(authorizationHeader);

        return iCapabilityHandler.create(request, token);
    }

}
