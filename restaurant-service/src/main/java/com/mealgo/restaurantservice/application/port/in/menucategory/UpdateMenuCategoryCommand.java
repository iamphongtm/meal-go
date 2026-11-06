package com.mealgo.restaurantservice.application.port.in.menucategory;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UpdateMenuCategoryCommand(
        UUID restaurantId,
        UUID ownerId,
        UUID categoryId,
        String name
) {
}
