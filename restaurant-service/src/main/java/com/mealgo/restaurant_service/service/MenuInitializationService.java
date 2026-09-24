package com.mealgo.restaurant_service.service;

import com.mealgo.restaurant_service.dto.request.CategoryCreationRequest;
import com.mealgo.restaurant_service.dto.request.MenuCreationRequest;
import com.mealgo.restaurant_service.dto.request.OptionCreationRequest;
import com.mealgo.restaurant_service.dto.request.OptionGroupCreationRequest;
import com.mealgo.restaurant_service.dto.response.CategoryCreationResponse;
import com.mealgo.restaurant_service.dto.response.OptionCreationResponse;
import com.mealgo.restaurant_service.dto.response.OptionGroupCreationResponse;

import java.util.UUID;

public interface MenuInitializationService {
    CategoryCreationResponse createCategory(UUID restaurantId, CategoryCreationRequest request);

    void initMenu(UUID restaurantId, MenuCreationRequest request);

    OptionGroupCreationResponse createOptionGroup(UUID menuItemId, OptionGroupCreationRequest request);

    OptionCreationResponse createOption(UUID optionGroupId, OptionCreationRequest request);
}
