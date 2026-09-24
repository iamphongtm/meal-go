package com.mealgo.restaurant_service.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record OptionCreationResponse(
        UUID id,
        String name,
        BigDecimal additionalPrice
) {
}
