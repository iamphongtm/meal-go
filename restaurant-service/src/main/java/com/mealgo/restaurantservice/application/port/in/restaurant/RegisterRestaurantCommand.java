package com.mealgo.restaurantservice.application.port.in.restaurant;

import java.util.UUID;

public record RegisterRestaurantCommand(
        UUID ownerId,
        String name,
        String phone,
        Integer provinceCode,
        Integer wardCode,
        String addressLine,
        Double latitude,
        Double longitude
) {
}
