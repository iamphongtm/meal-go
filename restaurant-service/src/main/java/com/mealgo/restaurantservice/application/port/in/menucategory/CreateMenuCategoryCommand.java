package com.mealgo.restaurantservice.application.port.in.menucategory;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateMenuCategoryCommand(
        UUID restaurantId,
        UUID ownerId,
        String name
) {
}
