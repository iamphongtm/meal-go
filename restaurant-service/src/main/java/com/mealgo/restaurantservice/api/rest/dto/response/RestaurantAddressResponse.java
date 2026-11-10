package com.mealgo.restaurantservice.api.rest.dto.response;

import java.util.UUID;

public record RestaurantAddressResponse(
        UUID restaurantId,
        Integer provinceCode,
        Integer wardCode,
        String addressLine,
        Double latitude,
        Double longitude
) {
}
