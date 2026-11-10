package com.mealgo.restaurantservice.application.port.in.restaurant;

import java.util.UUID;

public record UpdateRestaurantAddressCommand(
        UUID restaurantId,
        UUID ownerId,
        Integer provinceCode,
        Integer wardCode,
        String addressLine,
        Double latitude,
        Double longitude
) {
}
