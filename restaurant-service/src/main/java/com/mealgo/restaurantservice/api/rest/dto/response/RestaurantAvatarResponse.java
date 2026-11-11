package com.mealgo.restaurantservice.api.rest.dto.response;

import java.util.UUID;

public record RestaurantAvatarResponse(UUID restaurantId, String avatarUrl) {}
