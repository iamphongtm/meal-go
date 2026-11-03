package com.mealgo.restaurantservice.domain.model.restaurant;

import java.util.Objects;

public record Address(String addressLine, Integer wardCode) {
    public Address {
        Objects.requireNonNull(addressLine, "Address line must not be null");
        Objects.requireNonNull(wardCode, "Ward code must not be null");
    }
}
