package com.mealgo.restaurantservice.application.port.in.restaurant;

import com.mealgo.restaurantservice.domain.model.restaurant.RestaurantId;

public interface RegisterRestaurantUseCase {
    RestaurantId registerRestaurant(RegisterRestaurantCommand command);
}
