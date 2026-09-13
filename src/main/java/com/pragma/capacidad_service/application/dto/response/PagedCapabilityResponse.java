package com.pragma.capacidad_service.application.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record PagedCapabilityResponse(
        List<CapabilityListItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
