package com.pragma.capacidad_service.application.handler.impl;

import com.pragma.capacidad_service.application.dto.request.CapabilityRequest;
import com.pragma.capacidad_service.application.dto.response.CapabilityResponse;
import com.pragma.capacidad_service.application.handler.ICapabilityHandler;
import com.pragma.capacidad_service.application.mapper.CapabilityDtoMapper;
import com.pragma.capacidad_service.domain.api.ICapabilityRegisterServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CapabilityHandler implements ICapabilityHandler {

    private final ICapabilityRegisterServicePort iTechnologyRegisterServicePort;
    private final CapabilityDtoMapper technologyDtoMapper;

    @Override
    public Mono<CapabilityResponse> create(CapabilityRequest request, String token) {
        return iTechnologyRegisterServicePort.create(technologyDtoMapper.toCommand(request), token)
                .map(technologyDtoMapper::toResponse);
    }

}
