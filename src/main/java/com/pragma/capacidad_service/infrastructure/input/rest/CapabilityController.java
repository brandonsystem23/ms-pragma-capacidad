package com.pragma.capacidad_service.infrastructure.input.rest;

import com.pragma.capacidad_service.application.dto.request.CapabilityRequest;
import com.pragma.capacidad_service.application.dto.response.CapabilityResponse;
import com.pragma.capacidad_service.application.dto.response.PagedCapabilityResponse;
import com.pragma.capacidad_service.application.handler.ICapabilityHandler;
import com.pragma.capacidad_service.infrastructure.util.UtilTokenExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/capability")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Capacidad", description = "Endpoint para gestion de capacidades")
public class CapabilityController {

    private final ICapabilityHandler iCapabilityHandler;

    @PostMapping("/create")
    @Operation(summary = "Crear capacidad", description = "Crear una capacidad. Requiere rol ADMINISTRADOR")
    public Mono<CapabilityResponse> createCapability(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody CapabilityRequest request
    ) {

        log.info("Solicitud para crear una capacidad");

        String token = UtilTokenExtractor.extract(authorizationHeader);

        return iCapabilityHandler.create(request, token);
    }

    @GetMapping("/list")
    @Operation(summary = "Listar capacidades", description = "Listar capacidades paginadas. Requiere rol ADMINISTRADOR")
    public Mono<PagedCapabilityResponse> getCapabilities(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        log.info("Solicitud para listar capacidades");

        String token = UtilTokenExtractor.extract(authorizationHeader);

        return iCapabilityHandler.getCapabilities(page, size, sortBy, direction, token);
    }

    @PostMapping("/exists-by-ids")
    @Operation(summary = "Consultar capacidades existentes por ids",
            description = "Retorna los ids de las capacidades que existen. Requiere rol ADMINISTRADOR")
    public Mono<List<Long>> existsByIds(@RequestBody List<Long> ids) {

        log.info("Petición para validar tecnologías por ids");

        return iCapabilityHandler.existsByIds(ids);
    }


}
