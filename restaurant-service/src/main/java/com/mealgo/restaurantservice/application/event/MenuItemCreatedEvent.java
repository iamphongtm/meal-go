package com.mealgo.restaurantservice.application.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record MenuItemCreatedEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        UUID menuItemId,
        UUID restaurantId,
        UUID categoryId,
        String name,
        BigDecimal basePrice,
        String imageUrl,
        String status
) {}
