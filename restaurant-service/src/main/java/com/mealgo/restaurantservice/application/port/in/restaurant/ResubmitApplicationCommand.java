package com.mealgo.restaurantservice.application.port.in.restaurant;

import java.util.UUID;

public record ResubmitApplicationCommand(UUID restaurantId, UUID ownerId, String name, String phone,
        Integer provinceCode, Integer wardCode, String addressLine, Double latitude, Double longitude) {
}
