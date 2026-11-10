package com.mealgo.restaurantservice.application.event;

import java.time.Instant;
import java.util.UUID;

public record RestaurantAddressUpdatedEvent(
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
