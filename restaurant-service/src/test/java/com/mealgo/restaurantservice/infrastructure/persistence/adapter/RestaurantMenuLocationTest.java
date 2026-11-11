package com.mealgo.restaurantservice.infrastructure.persistence.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.mealgo.restaurantservice.domain.enums.ApprovalStatus;
import com.mealgo.restaurantservice.domain.enums.RestaurantStatus;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.AddressEmbeddable;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.RestaurantJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RestaurantMenuLocationTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void publicMenusSerializeStoredCoordinates(boolean tree) throws Exception {
        RestaurantJpaEntity restaurant = restaurant();
        restaurant.setLatitude(20.912345);
        restaurant.setLongitude(106.123456);

        JsonNode json = new ObjectMapper().valueToTree(menu(restaurant, tree)).get("restaurant");

        assertThat(json.get("latitude").doubleValue()).isEqualTo(20.912345);
        assertThat(json.get("longitude").doubleValue()).isEqualTo(106.123456);
        assertThat(json.get("acceptingOrders").booleanValue()).isFalse();
        assertThat(json.get("unavailableReason").textValue()).isEqualTo("RESTAURANT_CLOSED");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void missingCoordinatesRemainNull(boolean tree) {
        JsonNode json = new ObjectMapper().valueToTree(menu(restaurant(), tree)).get("restaurant");

        assertThat(json.get("latitude").isNull()).isTrue();
        assertThat(json.get("longitude").isNull()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void unapprovedRestaurantRemainsHidden(boolean tree) {
        RestaurantJpaEntity restaurant = restaurant();
        restaurant.setApprovalStatus(ApprovalStatus.PENDING);

        assertThat(menu(restaurant, tree)).isNull();
    }

    private RestaurantJpaEntity restaurant() {
        RestaurantJpaEntity restaurant = new RestaurantJpaEntity();
        restaurant.setId(UUID.randomUUID());
        restaurant.setName("Test restaurant");
        restaurant.setAddress(new AddressEmbeddable());
        restaurant.setApprovalStatus(ApprovalStatus.APPROVED);
        restaurant.setStatus(RestaurantStatus.CLOSED);
        return restaurant;
    }

    private Object menu(RestaurantJpaEntity restaurant, boolean tree) {
        RestaurantJpaRepository restaurants = mock(RestaurantJpaRepository.class);
        when(restaurants.findById(restaurant.getId())).thenReturn(Optional.of(restaurant));
        RestaurantMenuQueryAdapter adapter = new RestaurantMenuQueryAdapter(restaurants,
                mock(MenuCategoryJpaRepository.class), mock(MenuItemJpaRepository.class),
                mock(OptionGroupJpaRepository.class), mock(MenuItemOptionGroupJpaRepository.class));
        if (tree) {
            return adapter.findMenuTree(restaurant.getId()).orElse(null);
        }
        return adapter.findMenu(restaurant.getId()).orElse(null);
    }
}
