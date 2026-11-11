package com.mealgo.restaurantservice.application.port.in.restaurant;

import java.util.UUID;

public record RestaurantAvatarResult(UUID restaurantId, String avatarUrl) {}
