package com.mealgo.restaurantservice.application.port.in.menu;

import java.util.List;
import java.util.UUID;

public record ValidateMenuQuery(
        UUID restaurantId,
        List<Item> items
) {
    public record Item(
            UUID lineId,
            UUID menuItemId,
            int quantity,
            List<UUID> selectedOptionIds
    ) {
    }
}