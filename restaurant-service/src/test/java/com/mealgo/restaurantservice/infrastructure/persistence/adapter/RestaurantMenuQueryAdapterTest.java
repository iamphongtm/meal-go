package com.mealgo.restaurantservice.infrastructure.persistence.adapter;

import com.mealgo.restaurantservice.domain.enums.*;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.*;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestaurantMenuQueryAdapterTest {
    private final RestaurantJpaRepository restaurants = mock(RestaurantJpaRepository.class);
    private final MenuCategoryJpaRepository categories = mock(MenuCategoryJpaRepository.class);
    private final MenuItemJpaRepository items = mock(MenuItemJpaRepository.class);
    private final OptionGroupJpaRepository groups = mock(OptionGroupJpaRepository.class);
    private final RestaurantMenuQueryAdapter adapter =
            new RestaurantMenuQueryAdapter(restaurants, categories, items, groups);
    private final UUID restaurantId = UUID.randomUUID();
    private final UUID itemId = UUID.randomUUID();

    @Test
    void returnsEmptyForUnknownRestaurant() {
        when(restaurants.findById(restaurantId)).thenReturn(Optional.empty());
        assertTrue(adapter.findMenu(restaurantId).isEmpty());
        verifyNoInteractions(categories, items, groups);
    }

    @Test
    void closedRestaurantRemainsVisibleAndInactiveItemsAreHidden() {
        var restaurant = restaurant(RestaurantStatus.CLOSED);
        var category = category(restaurant);
        var active = item(category, MenuItemStatus.ACTIVE);
        var soldOut = item(category, MenuItemStatus.SOLD_OUT);
        var inactive = item(category, MenuItemStatus.INACTIVE);
        category.setItems(List.of(active, soldOut, inactive));
        when(restaurants.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(categories.findCustomerMenu(restaurantId)).thenReturn(List.of(category));
        when(items.findItemIdsWithActiveOptions(restaurantId)).thenReturn(List.of(itemId));

        var result = adapter.findMenu(restaurantId).orElseThrow();

        assertFalse(result.restaurant().acceptingOrders());
        assertEquals("RESTAURANT_CLOSED", result.restaurant().unavailableReason());
        assertEquals(2, result.categories().getFirst().items().size());
        assertTrue(result.categories().getFirst().items().getFirst().hasOptions());
        assertFalse(result.categories().getFirst().items().get(1).available());
    }

    @Test
    void emptyMenuUsesEmptyCategories() {
        when(restaurants.findById(restaurantId)).thenReturn(Optional.of(restaurant(RestaurantStatus.OPENING)));
        when(categories.findCustomerMenu(restaurantId)).thenReturn(List.of());
        when(items.findItemIdsWithActiveOptions(restaurantId)).thenReturn(List.of());
        assertEquals(List.of(), adapter.findMenu(restaurantId).orElseThrow().categories());
    }

    @Test
    void returnsOnlyConfiguredOptionsWithPerDishPrices() {
        var item = item(category(restaurant(RestaurantStatus.OPENING)), MenuItemStatus.ACTIVE);
        var definition = definition();
        var active = option(OptionStatus.ACTIVE);
        var soldOut = option(OptionStatus.SOLD_OUT);
        var inactive = option(OptionStatus.INACTIVE);
        var unconfigured = option(OptionStatus.ACTIVE);
        definition.setOptions(List.of(active, soldOut, inactive, unconfigured));
        var assignment = assignment(definition, 1);
        assignment.setOptionPrices(List.of(price(active, "15000"), price(soldOut, "20000"), price(inactive, "0")));
        item.setOptionGroups(List.of(assignment));
        stub(item, definition);

        var result = adapter.findMenuItem(restaurantId, itemId).orElseThrow();
        var group = result.optionGroups().getFirst();

        assertTrue(result.available());
        assertEquals("VND", result.currency());
        assertEquals(1, group.minSelections());
        assertEquals(2, group.options().size());
        assertEquals(new BigDecimal("15000"), group.options().getFirst().additionalPrice());
        assertFalse(group.options().get(1).available());
    }

    @Test
    void requiredGroupWithoutEnoughAvailableOptionsMakesItemUnavailable() {
        var item = item(category(restaurant(RestaurantStatus.OPENING)), MenuItemStatus.ACTIVE);
        var definition = definition();
        var soldOut = option(OptionStatus.SOLD_OUT);
        definition.setOptions(List.of(soldOut));
        var assignment = assignment(definition, 1);
        assignment.setOptionPrices(List.of(price(soldOut, "0")));
        item.setOptionGroups(List.of(assignment));
        stub(item, definition);
        assertFalse(adapter.findMenuItem(restaurantId, itemId).orElseThrow().available());
    }

    @Test
    void inactiveRequiredGroupMakesItemUnavailable() {
        var item = item(category(restaurant(RestaurantStatus.OPENING)), MenuItemStatus.ACTIVE);
        var definition = definition();
        definition.setStatus(OptionGroupStatus.INACTIVE);
        item.setOptionGroups(List.of(assignment(definition, 1)));
        stub(item, definition);
        var result = adapter.findMenuItem(restaurantId, itemId).orElseThrow();
        assertFalse(result.available());
        assertTrue(result.optionGroups().isEmpty());
    }

    @Test
    void itemInClosedRestaurantIsUnavailable() {
        var item = item(category(restaurant(RestaurantStatus.CLOSED)), MenuItemStatus.ACTIVE);
        when(items.findCustomerItem(restaurantId, itemId)).thenReturn(Optional.of(item));
        var result = adapter.findMenuItem(restaurantId, itemId).orElseThrow();
        assertFalse(result.available());
        assertTrue(result.optionGroups().isEmpty());
        verifyNoInteractions(groups);
    }

    @Test
    void missingOrWrongRestaurantItemIsNotReturned() {
        when(items.findCustomerItem(restaurantId, itemId)).thenReturn(Optional.empty());
        assertTrue(adapter.findMenuItem(restaurantId, itemId).isEmpty());
        verifyNoInteractions(groups);
    }

    private void stub(MenuItemJpaEntity item, OptionGroupJpaEntity definition) {
        when(items.findCustomerItem(restaurantId, itemId)).thenReturn(Optional.of(item));
        when(groups.findByRestaurantIdAndIdIn(restaurantId, List.of(definition.getId())))
                .thenReturn(List.of(definition));
    }

    private RestaurantJpaEntity restaurant(RestaurantStatus status) {
        var restaurant = new RestaurantJpaEntity();
        restaurant.setId(restaurantId);
        restaurant.setName("Bún bò Huế");
        restaurant.setStatus(status);
        var address = new AddressEmbeddable();
        address.setAddressLine("12 Nguyễn Tri Phương");
        restaurant.setAddress(address);
        return restaurant;
    }

    private MenuCategoryJpaEntity category(RestaurantJpaEntity restaurant) {
        var category = new MenuCategoryJpaEntity();
        category.setId(UUID.randomUUID());
        category.setRestaurant(restaurant);
        category.setStatus(CategoryStatus.ACTIVE);
        return category;
    }

    private MenuItemJpaEntity item(MenuCategoryJpaEntity category, MenuItemStatus status) {
        var item = new MenuItemJpaEntity();
        item.setId(itemId);
        item.setCategory(category);
        item.setName("Bún bò đặc biệt");
        item.setBasePrice(new BigDecimal("65000"));
        item.setStatus(status);
        return item;
    }

    private OptionGroupJpaEntity definition() {
        var group = new OptionGroupJpaEntity();
        group.setId(UUID.randomUUID());
        group.setName("Size");
        group.setStatus(OptionGroupStatus.ACTIVE);
        return group;
    }

    private MenuItemOptionGroupJpaEntity assignment(OptionGroupJpaEntity definition, int min) {
        var assignment = new MenuItemOptionGroupJpaEntity();
        assignment.setOptionGroupId(definition.getId());
        assignment.setMinSelect(min);
        assignment.setMaxSelect(1);
        return assignment;
    }

    private OptionJpaEntity option(OptionStatus status) {
        var option = new OptionJpaEntity();
        option.setId(UUID.randomUUID());
        option.setName("Option");
        option.setStatus(status);
        return option;
    }

    private MenuItemOptionPriceJpaEntity price(OptionJpaEntity option, String amount) {
        var price = new MenuItemOptionPriceJpaEntity();
        price.setOptionId(option.getId());
        price.setAdditionalPrice(new BigDecimal(amount));
        return price;
    }
}
