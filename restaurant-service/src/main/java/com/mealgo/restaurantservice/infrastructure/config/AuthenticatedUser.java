package com.mealgo.restaurantservice.infrastructure.config;

import java.util.UUID;

public record AuthenticatedUser(
        UUID userId,
        String email
) {
}
