package com.mealgo.restaurantservice.infrastructure.persistence.mapper;


import com.mealgo.restaurantservice.domain.model.restaurant.Address;
import com.mealgo.restaurantservice.domain.model.restaurant.Restaurant;
import com.mealgo.restaurantservice.domain.model.restaurant.RestaurantId;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.AddressEmbeddable;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.RestaurantJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.WardJpaEntity;

public final class RestaurantPersistenceMapper {
    private RestaurantPersistenceMapper() {
    }

    public static Restaurant toDomain(RestaurantJpaEntity entity) {
        var address = entity.getAddress();

        return new Restaurant(
                RestaurantId.of(entity.getId()),
                entity.getOwnerId(),
                entity.getName(),
                entity.getPhone(),
                entity.getStatus(),
                new Address(
                        address.getAddressLine(),
                        address.getWard().getCode()
                ),
                entity.getLatitude(),
                entity.getLongitude()
        );
    }

    public static RestaurantJpaEntity toNewEntity(
            Restaurant restaurant,
            WardJpaEntity wardReference
    ) {
        var address = new AddressEmbeddable();
        address.setAddressLine(restaurant.getAddress().addressLine());
        address.setWard(wardReference);

        var entity = new RestaurantJpaEntity();
        entity.setId(restaurant.getId().value());
        entity.setOwnerId(restaurant.getOwnerId());
        entity.setName(restaurant.getName());
        entity.setPhone(restaurant.getPhone());
        entity.setStatus(restaurant.getStatus());
        entity.setAddress(address);
        entity.setLatitude(restaurant.getLatitude());
        entity.setLongitude(restaurant.getLongitude());

        return entity;
    }

}
