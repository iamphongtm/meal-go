package com.mealgo.restaurantservice.application.port.in.restaurant;

import java.util.UUID;

public interface GetMyRestaurantUseCase {
    MyRestaurantResult getMyRestaurant(UUID ownerId);
}
