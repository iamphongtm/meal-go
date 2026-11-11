package com.mealgo.restaurantservice.application.port.in.restaurant;

import java.util.UUID;

public record UpdateAvatarCommand(UUID restaurantId, UUID ownerId, String avatarUrl) {}
