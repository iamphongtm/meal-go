package com.mealgo.searchservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class MenuItemDocument {
    private final UUID menuItemId;
    private final UUID restaurantId;
    private final String name;
    private final BigDecimal basePrice;
    private final String imageUrl;
    private final String status;
    private final Instant updatedAt;

}
