package com.mealgo.restaurantservice.application.port.out;

import com.mealgo.restaurantservice.application.port.in.menu.MenuItemDetailResult;
import com.mealgo.restaurantservice.application.port.in.menu.RestaurantMenuResult;
import com.mealgo.restaurantservice.application.port.in.menu.RestaurantMenuTreeResult;

import java.util.Optional;
import java.util.UUID;

public interface RestaurantMenuQueryPort {
    Optional<RestaurantMenuTreeResult> findMenuTree(UUID restaurantId);

    Optional<RestaurantMenuResult> findMenu(UUID restaurantId);

    Optional<MenuItemDetailResult> findMenuItem(UUID restaurantId, UUID menuItemId);
}
