package com.mealgo.restaurantservice.domain.model.restaurant;

import com.mealgo.restaurantservice.domain.enums.RestaurantStatus;
import com.mealgo.restaurantservice.domain.shared.Entity;
import com.mealgo.restaurantservice.domain.shared.IAggregateRoot;
import lombok.Getter;

import java.util.UUID;

@Getter
public final class Restaurant extends Entity<RestaurantId> implements IAggregateRoot {
    private final UUID ownerId;
    private final String name;
    private final String phone;
    private final RestaurantStatus status;
    private final Address address;
    private final Double latitude;
    private final Double longitude;

    public Restaurant(RestaurantId id, UUID ownerId, String name, String phone,
                      RestaurantStatus status, Address address, Double latitude, Double longitude) {
        super(id);
        this.ownerId = ownerId;
        this.name = name;
        this.phone = phone;
        this.status = status;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static Restaurant register(UUID ownerId, String name, String phone, RestaurantStatus status,
                                      Address address, Double latitude, Double longitude) {
        return new Restaurant(RestaurantId.newId(), ownerId, name, phone, status, address, latitude, longitude);
    }

}
