package com.mealgo.restaurantservice.application.event;

import java.time.Instant;
import java.util.UUID;

public record RestaurantApprovedEvent(UUID eventId, String eventType, Instant occurredAt,
        UUID restaurantId, UUID ownerId, UUID reviewedBy) {
}
