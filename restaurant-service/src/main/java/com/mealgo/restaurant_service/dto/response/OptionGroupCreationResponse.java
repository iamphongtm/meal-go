package com.mealgo.restaurant_service.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record OptionGroupCreationResponse(
        UUID id,
        String name,
        Integer minSelect,
        Integer maxSelect
) {
}
