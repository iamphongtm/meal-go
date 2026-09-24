package com.mealgo.restaurant_service.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CategoryCreationResponse(
        UUID id,
        String name,
        Integer displayOrder
) {
}
