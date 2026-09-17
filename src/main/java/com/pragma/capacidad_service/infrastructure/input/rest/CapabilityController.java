package com.pragma.capacidad_service.infrastructure.input.rest;

import com.pragma.capacidad_service.application.dto.request.CapabilityFilterDto;
import com.pragma.capacidad_service.application.dto.request.CapabilityRequest;
import com.pragma.capacidad_service.application.dto.response.CapabilityListItemResponse;
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
import reactor.core.publisher.Flux;
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
            @ModelAttribute CapabilityFilterDto filter
    ) {
        log.info("Solicitud para listar capacidades. Página: {}, Tamaño: {}", filter.page(), filter.size());

        String token = UtilTokenExtractor.extract(authorizationHeader);

        return iCapabilityHandler.getCapabilities(filter, token);
    }

    @PostMapping("/exists-by-ids")
    @Operation(summary = "Consultar capacidades existentes por ids",
            description = "Retorna los ids de las capacidades que existen. Requiere rol ADMINISTRADOR")
    public Mono<List<Long>> existsByIds(@RequestBody List<Long> ids) {

        log.info("Petición para validar tecnologías por ids");

        return iCapabilityHandler.existsByIds(ids);
    }

    @GetMapping("/by-ids")
    @Operation(summary = "Obtener capacidades por ids",
            description = "Retorna las capacidades encontradas según la lista de ids. Requiere rol ADMINISTRADOR")
    public Flux<CapabilityListItemResponse> findByIds(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam List<Long> ids) {

        log.info("Petición para obtener capacidades por ids");

        String token = UtilTokenExtractor.extract(authorizationHeader);

        return iCapabilityHandler.findByIds(ids, token);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Eliminar capacidades por ids",
            description = "Elimina capacidades y sus relaciones, luego invoca el borrado de tecnologías. Requiere rol ADMINISTRADOR")
    public Mono<Void> deleteByIds(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody List<Long> ids
    ) {
        log.info("Petición para eliminar capacidades por ids");

        String token = UtilTokenExtractor.extract(authorizationHeader);

        return iCapabilityHandler.deleteByIds(ids, token);
    }
}
