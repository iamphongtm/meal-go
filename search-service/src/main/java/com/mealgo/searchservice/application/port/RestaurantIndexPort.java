package com.mealgo.searchservice.application.port;

import com.mealgo.searchservice.domain.MenuItemDocument;
import com.mealgo.searchservice.domain.RestaurantDocument;

public interface RestaurantIndexPort {
    void upsertRestaurant(RestaurantDocument restaurant);
    void upsertMenuItem(MenuItemDocument menuItem);
}
