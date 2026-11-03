package com.mealgo.restaurantservice.api.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record RegisterRestaurantResponse(
        @Schema(description = "Identifier of the registered restaurant",
                example = "7f901364-615c-4176-a587-1045ec26981f")
        UUID restaurantId
) {
}
