package com.mealgo.restaurantservice.domain.model.category;

import java.util.Objects;
import java.util.UUID;

public record MenuCategoryId(UUID value) {
    public MenuCategoryId {
        Objects.requireNonNull(value, "Menu category ID must not be null");
    }

    public static MenuCategoryId newId() {
        return new MenuCategoryId(UUID.randomUUID());
    }

    public static MenuCategoryId of(UUID value) {
        return new MenuCategoryId(value);
    }

    public static MenuCategoryId of(String value) {
        return of(UUID.fromString(value));
    }
}
