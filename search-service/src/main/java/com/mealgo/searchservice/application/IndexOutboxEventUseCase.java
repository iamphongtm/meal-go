package com.mealgo.searchservice.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealgo.searchservice.application.port.CatalogIndexPort;
import com.mealgo.searchservice.domain.CatalogEventType;
import com.mealgo.searchservice.domain.MenuItemDocument;
import com.mealgo.searchservice.domain.RestaurantDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IndexOutboxEventUseCase {

    private final CatalogIndexPort indexPort;
    private final ObjectMapper objectMapper;

    public void handle(CatalogEventType eventType, UUID aggregateId, Instant occurredAt, JsonNode payload) {
        switch (eventType) {
            case RESTAURANT_CREATED -> indexRestaurant(aggregateId, occurredAt, payload);
            case MENU_ITEM_CREATED -> indexMenuItem(aggregateId, occurredAt, payload);
            default -> throw new IllegalArgumentException("Unsupported event: " + eventType);
        }
    }

    private void indexRestaurant(UUID aggregateId, Instant occurredAt, JsonNode payload) {
        RestaurantEvent event = objectMapper.convertValue(payload, RestaurantEvent.class);
        requireIdentity(aggregateId, event.restaurantId());
        requireText(event.name(), "name");
        requireText(event.status(), "status");

        indexPort.upsert(RestaurantDocument.builder()
                .restaurantId(event.restaurantId())
                .name(event.name())
                .status(event.status())
                .addressLine(event.addressLine())
                .wardCode(event.wardCode())
                .latitude(event.latitude())
                .longitude(event.longitude())
                .updatedAt(occurredAt)
                .build());
    }

    private void indexMenuItem(UUID aggregateId, Instant occurredAt, JsonNode payload) {
        MenuItemEvent event = objectMapper.convertValue(payload, MenuItemEvent.class);
        requireIdentity(aggregateId, event.menuItemId());
        requireText(event.name(), "name");
        requireText(event.status(), "status");

        if (event.restaurantId() == null || event.categoryId() == null || event.basePrice() == null) {
            throw new IllegalArgumentException("Menu item requires restaurantId, categoryId and basePrice");
        }

        indexPort.upsert(MenuItemDocument.builder()
                .menuItemId(event.menuItemId())
                .restaurantId(event.restaurantId())
                .categoryId(event.categoryId())
                .name(event.name())
                .basePrice(event.basePrice())
                .imageUrl(event.imageUrl())
                .status(event.status())
                .updatedAt(occurredAt)
                .build());
    }

    private void requireIdentity(UUID aggregateId, UUID entityId) {
        if (aggregateId == null || !aggregateId.equals(entityId)) {
            throw new IllegalArgumentException("Event entity ID does not match outbox aggregate ID");
        }
    }

    private void requireText(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Missing field: " + field);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RestaurantEvent(
            UUID restaurantId,
            String name,
            String status,
            String addressLine,
            Integer wardCode,
            Double latitude,
            Double longitude
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MenuItemEvent(UUID menuItemId,
                                UUID restaurantId,
                                UUID categoryId,
                                String name,
                                BigDecimal basePrice,
                                String imageUrl,
                                 String status) { }
}
