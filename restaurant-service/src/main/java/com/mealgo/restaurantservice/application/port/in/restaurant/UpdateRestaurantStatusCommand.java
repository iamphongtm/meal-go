package com.mealgo.restaurantservice.application.port.in.restaurant;

import com.mealgo.restaurantservice.domain.enums.RestaurantStatus;
import java.util.UUID;

public record UpdateRestaurantStatusCommand(UUID restaurantId, UUID ownerId,
                                            RestaurantStatus status, String pauseReason) {
}
