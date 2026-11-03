package com.mealgo.searchservice.domain;

import java.util.Optional;

public enum CatalogEventType {
    RESTAURANT_CREATED,
    MENU_ITEM_CREATED;

    public static Optional<CatalogEventType> fromExternal(String value) {
        if (value == null) {
            return Optional.empty();
        }

        return switch (value) {
            case "RestaurantCreated" -> Optional.of(RESTAURANT_CREATED);
            case "MenuItemCreated" -> Optional.of(MENU_ITEM_CREATED);
            default -> Optional.empty();
        };
    }
}
