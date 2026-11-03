package com.mealgo.restaurantservice.application.port.in.menu;

import java.util.UUID;

public interface GetRestaurantMenuUseCase {
    RestaurantMenuResult getMenu(UUID restaurantId);

    MenuItemDetailResult getMenuItem(UUID restaurantId, UUID menuItemId);
}
