package com.mealgo.restaurantservice.application.port.in.restaurant;

import com.mealgo.restaurantservice.domain.enums.RestaurantStatus;
import java.util.UUID;

public record RestaurantStatusResult(UUID restaurantId, RestaurantStatus status, String pauseReason) {
}
