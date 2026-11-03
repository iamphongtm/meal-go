package com.mealgo.restaurantservice.application.port.out;

import com.mealgo.restaurantservice.application.port.in.menu.MenuItemDetailResult;
import com.mealgo.restaurantservice.application.port.in.menu.RestaurantMenuResult;

import java.util.Optional;
import java.util.UUID;

public interface RestaurantMenuQueryPort {
    Optional<RestaurantMenuResult> findMenu(UUID restaurantId);

    Optional<MenuItemDetailResult> findMenuItem(UUID restaurantId, UUID menuItemId);
}
