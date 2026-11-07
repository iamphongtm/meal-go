package com.mealgo.searchservice.repository;

import com.mealgo.searchservice.model.MenuItemDocument;
import com.mealgo.searchservice.model.RestaurantDocument;

public interface RestaurantIndexRepository {
    void upsertRestaurant(RestaurantDocument restaurant);
    void upsertMenuItem(MenuItemDocument menuItem);
}
