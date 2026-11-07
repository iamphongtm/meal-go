package com.mealgo.searchservice.repository;

import com.mealgo.searchservice.dto.RestaurantSearchPage;

public interface RestaurantSearchRepository {
    RestaurantSearchPage findNearby(double latitude, double longitude, double radiusKm, int page, int size);

    RestaurantSearchPage findByDish(String keyword, int page, int size);
}
