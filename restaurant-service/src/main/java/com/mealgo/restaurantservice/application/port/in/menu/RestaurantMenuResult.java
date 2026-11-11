package com.mealgo.restaurantservice.application.port.in.menu;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RestaurantMenuResult(RestaurantSummary restaurant, List<Category> categories) {
    public record RestaurantSummary(UUID restaurantId, String name, String addressLine,
                                    Double latitude, Double longitude, String avatarUrl,
                                    boolean acceptingOrders, String unavailableReason) {}

    public record Category(UUID categoryId, String name, Integer displayOrder, List<Item> items) {}

    public record Item(UUID menuItemId, String name, String description, BigDecimal basePrice,
                       String imageUrl, boolean available, boolean hasOptions) {}
}
