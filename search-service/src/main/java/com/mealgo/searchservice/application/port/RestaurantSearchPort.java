package com.mealgo.searchservice.application.port;

import com.mealgo.searchservice.application.model.RestaurantSearchPage;

public interface RestaurantSearchPort {
    RestaurantSearchPage findNearby(double latitude, double longitude, double radiusKm, int page, int size);

    RestaurantSearchPage findByDish(String keyword, int page, int size);
}
