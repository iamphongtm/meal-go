package com.mealgo.searchservice.model;

import java.util.Optional;

public enum CatalogEventType {
    RESTAURANT_CREATED,
    RESTAURANT_ADDRESS_UPDATED,
    MENU_ITEM_CREATED;

    public static Optional<CatalogEventType> fromExternal(String value) {
        if (value == null) {
            return Optional.empty();
        }

        return switch (value) {
            case "RestaurantCreated" -> Optional.of(RESTAURANT_CREATED);
            case "RestaurantAddressUpdated" -> Optional.of(RESTAURANT_ADDRESS_UPDATED);
            case "MenuItemCreated" -> Optional.of(MENU_ITEM_CREATED);
            default -> Optional.empty();
        };
    }
}
