package com.mealgo.restaurantservice.domain.model.menuitem;

import java.util.Objects;
import java.util.UUID;

public record MenuItemId(UUID value) {
    public MenuItemId {
        Objects.requireNonNull(value, "Menu item ID must not be null");
    }

    public static MenuItemId newId() {
        return new MenuItemId(UUID.randomUUID());
    }

    public static MenuItemId of(UUID value) {
        return new MenuItemId(value);
    }

    public static MenuItemId of(String value) {
        return of(UUID.fromString(value));
    }
}
