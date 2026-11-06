package com.mealgo.restaurantservice.application.port.in.restaurant;

import com.mealgo.restaurantservice.domain.enums.RestaurantStatus;

import java.util.UUID;

public record MyRestaurantResult(UUID restaurantId, String name, RestaurantStatus status, String pauseReason) {
    public MyRestaurantResult(UUID restaurantId, String name, RestaurantStatus status) {
        this(restaurantId, name, status, null);
    }
}
