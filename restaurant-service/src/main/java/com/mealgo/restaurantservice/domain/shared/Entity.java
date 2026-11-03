package com.mealgo.restaurantservice.domain.shared;

import java.util.Objects;

public abstract class Entity<T> {

    private final T id;

    protected Entity(T id) {
        this.id = Objects.requireNonNull(id, "Entity ID must not be null");
    }

    public final T getId() {
        return id;
    }
}
