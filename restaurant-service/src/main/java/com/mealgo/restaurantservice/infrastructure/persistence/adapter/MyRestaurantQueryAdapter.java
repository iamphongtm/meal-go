package com.mealgo.restaurantservice.infrastructure.persistence.adapter;

import com.mealgo.restaurantservice.application.port.in.restaurant.MyRestaurantResult;
import com.mealgo.restaurantservice.application.port.out.MyRestaurantQueryPort;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.RestaurantJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyRestaurantQueryAdapter implements MyRestaurantQueryPort {
    private final RestaurantJpaRepository restaurantJpaRepository;

    @Override
    public Optional<MyRestaurantResult> findByOwnerId(UUID ownerId) {
        return restaurantJpaRepository.findByOwnerId(ownerId)
                .map(restaurant -> new MyRestaurantResult(
                        restaurant.getId(), restaurant.getName(), restaurant.getStatus()));
    }
}
