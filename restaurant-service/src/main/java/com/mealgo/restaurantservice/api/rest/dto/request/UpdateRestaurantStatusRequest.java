package com.mealgo.restaurantservice.api.rest.dto.request;

import com.mealgo.restaurantservice.domain.enums.RestaurantStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRestaurantStatusRequest(@NotNull RestaurantStatus status, @Size(max = 30) String pauseReason) {
}
