package com.mealgo.restaurantservice.application.port.out;

import com.mealgo.restaurantservice.domain.model.restaurant.Restaurant;
import com.mealgo.restaurantservice.domain.enums.RestaurantStatus;

import java.util.UUID;
import java.util.Optional;

public interface RestaurantRepository {
    boolean updateStatus(UUID id, UUID ownerId, RestaurantStatus status, String pauseReason);
    void save(Restaurant restaurant);

    boolean existByOwnerId(UUID ownerId);

    boolean existsByIdAndOwnerId(UUID id, UUID restaurantId);

    Optional<String> findNameByIdAndOwnerId(UUID id, UUID ownerId);
}
