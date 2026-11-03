package com.mealgo.restaurantservice.domain.model.optiongroup;

import java.util.Objects;
import java.util.UUID;

public record OptionGroupId(UUID value) {
    public OptionGroupId {
        Objects.requireNonNull(value, "Option group ID must not be null");
    }

    public static OptionGroupId newId() {
        return new OptionGroupId(UUID.randomUUID());
    }

    public static OptionGroupId of(UUID value) {
        return new OptionGroupId(value);
    }

    public static OptionGroupId of(String value) {
        return of(UUID.fromString(value));
    }
}
