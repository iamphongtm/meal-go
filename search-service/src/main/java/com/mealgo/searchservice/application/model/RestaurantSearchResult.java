package com.mealgo.searchservice.application.model;

import java.math.BigDecimal;
import java.util.List;

public record RestaurantSearchResult(
        String restaurantId,
        String name,
        String status,
        String addressLine,
        Double distanceKm,
        List<MatchedMenuItem> matchedMenuItems
) {
    public record MatchedMenuItem(
            String menuItemId,
            String name,
            String status,
            BigDecimal basePrice,
            String imageUrl
    ) {
    }
}
