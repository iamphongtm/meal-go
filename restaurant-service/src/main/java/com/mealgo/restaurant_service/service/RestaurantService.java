package com.mealgo.restaurant_service.service;

import com.mealgo.restaurant_service.dto.request.RestaurantCreationRequest;

public interface RestaurantService {
    void init(RestaurantCreationRequest request, String ownerId);

}
