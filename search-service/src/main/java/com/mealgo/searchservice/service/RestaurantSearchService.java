package com.mealgo.searchservice.service;

import com.mealgo.searchservice.dto.RestaurantSearchPage;

public interface RestaurantSearchService {
    RestaurantSearchPage findNearby(double latitude, double longitude, double radiusKm, int page, int size);

    RestaurantSearchPage findByDish(String keyword, int page, int size);
}
