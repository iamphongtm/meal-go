package com.mealgo.restaurantservice.application.port.out;

import com.mealgo.restaurantservice.application.port.in.restaurant.MyRestaurantResult;

import java.util.Optional;
import java.util.UUID;

public interface MyRestaurantQueryPort {
    Optional<MyRestaurantResult> findByOwnerId(UUID ownerId);
}
