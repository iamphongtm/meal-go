package com.mealgo.restaurant_service.domain.repository;

import com.mealgo.restaurant_service.domain.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
    Restaurant findByOwnerId(UUID ownerId);

    boolean existsByOwnerId(UUID ownerId);

    Optional<Restaurant> findByIdAndOwnerId(UUID id, UUID ownerId);
}
