package com.mealgo.restaurantservice.application.event;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record RestaurantCreatedEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        UUID restaurantId,
        String name,
        String status,
        String addressLine,
        Integer wardCode,
        Double latitude,
        Double longitude
) {
}
