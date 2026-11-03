package com.mealgo.restaurantservice.application.port.out;

import com.mealgo.restaurantservice.domain.model.restaurant.Restaurant;

import java.util.UUID;
import java.util.Optional;

public interface RestaurantRepository {
    void save(Restaurant restaurant);

    boolean existByOwnerId(UUID ownerId);

    boolean existsByIdAndOwnerId(UUID id, UUID restaurantId);

    Optional<String> findNameByIdAndOwnerId(UUID id, UUID ownerId);
}
