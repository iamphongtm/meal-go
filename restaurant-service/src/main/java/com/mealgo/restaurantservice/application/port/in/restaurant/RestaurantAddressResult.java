package com.mealgo.restaurantservice.application.port.in.restaurant;

import java.util.UUID;

public record RestaurantAddressResult(
        UUID restaurantId,
        Integer provinceCode,
        Integer wardCode,
        String addressLine,
        Double latitude,
        Double longitude
) {
}
