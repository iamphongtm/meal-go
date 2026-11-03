package com.mealgo.searchservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RestaurantDocument implements CatalogDocument {
    private final UUID restaurantId;
    private final String name;
    private final String status;
    private final String addressLine;
    private final Integer wardCode;
    private final Double latitude;
    private final Double longitude;
    private final Instant updatedAt;


    @Override
    public CatalogType type() {
        return CatalogType.RESTAURANT;
    }

    @Override
    public UUID entityId() {
        return restaurantId;
    }

    @Override
    public String name() {
        return name;
    }
}
