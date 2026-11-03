package com.mealgo.restaurantservice.domain.model.restaurant;

import java.util.Objects;
import java.util.UUID;

public record RestaurantId(UUID value) {
    public RestaurantId {
        Objects.requireNonNull(value, "Restaurant ID must not be null");
    }

    public static RestaurantId newId() {
        return new RestaurantId(UUID.randomUUID());
    }

    public static RestaurantId of(UUID value) {
        return new RestaurantId(value);
    }

    public static RestaurantId of(String value) {
        return of(UUID.fromString(value));
    }
}
