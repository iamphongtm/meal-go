package com.mealgo.restaurant_service.service;

import com.mealgo.restaurant_service.dto.request.RestaurantCreationRequest;

import java.util.UUID;

public interface RestaurantService {
    void init(RestaurantCreationRequest request, UUID ownerId);
}
