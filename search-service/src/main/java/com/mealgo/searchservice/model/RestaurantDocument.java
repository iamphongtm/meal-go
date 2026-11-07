package com.mealgo.searchservice.model;

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
public class RestaurantDocument {
    private final UUID restaurantId;
    private final String name;
    private final String status;
    private final String addressLine;
    private final Double latitude;
    private final Double longitude;
    private final Instant updatedAt;


}
