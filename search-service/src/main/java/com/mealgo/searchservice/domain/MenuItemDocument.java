package com.mealgo.searchservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class MenuItemDocument implements CatalogDocument {
    private final UUID menuItemId;
    private final UUID restaurantId;
    private final UUID categoryId;
    private final String name;
    private final BigDecimal basePrice;
    private final String imageUrl;
    private final String status;
    private final Instant updatedAt;

    @Override
    public CatalogType type() {
        return CatalogType.MENU_ITEM;
    }

    @Override
    public UUID entityId() {
        return menuItemId;
    }

    @Override
    public String name() {
        return name;
    }
}
