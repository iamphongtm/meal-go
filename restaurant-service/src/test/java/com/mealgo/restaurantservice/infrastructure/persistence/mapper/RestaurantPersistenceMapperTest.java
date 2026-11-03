package com.mealgo.restaurantservice.infrastructure.persistence.mapper;

import com.mealgo.restaurantservice.domain.enums.RestaurantStatus;
import com.mealgo.restaurantservice.domain.model.restaurant.Address;
import com.mealgo.restaurantservice.domain.model.restaurant.Restaurant;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.WardJpaEntity;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestaurantPersistenceMapperTest {

    @Test
    void preservesCoordinatesWhenMappingToEntityAndBack() {
        var restaurant = restaurant(10.7626, 106.6822);
        var entity = RestaurantPersistenceMapper.toNewEntity(restaurant, ward());

        assertEquals(restaurant.getLatitude(), entity.getLatitude());
        assertEquals(restaurant.getLongitude(), entity.getLongitude());

        var restored = RestaurantPersistenceMapper.toDomain(entity);
        assertEquals(restaurant.getLatitude(), restored.getLatitude());
        assertEquals(restaurant.getLongitude(), restored.getLongitude());
    }

    @Test
    void supportsExistingRestaurantsWithoutCoordinates() {
        var entity = RestaurantPersistenceMapper.toNewEntity(restaurant(null, null), ward());
        var restored = RestaurantPersistenceMapper.toDomain(entity);

        assertNull(restored.getLatitude());
        assertNull(restored.getLongitude());
    }

    private Restaurant restaurant(Double latitude, Double longitude) {
        return Restaurant.register(UUID.randomUUID(), "Restaurant", "0901234567",
                RestaurantStatus.CLOSED, new Address("123 Nguyen Trai", 12345),
                latitude, longitude);
    }

    private WardJpaEntity ward() {
        var ward = mock(WardJpaEntity.class);
        when(ward.getCode()).thenReturn(12345);
        return ward;
    }
}
