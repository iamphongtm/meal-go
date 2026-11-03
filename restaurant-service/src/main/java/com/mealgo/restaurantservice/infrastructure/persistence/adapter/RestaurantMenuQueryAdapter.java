package com.mealgo.restaurantservice.infrastructure.persistence.adapter;

import com.mealgo.restaurantservice.application.port.in.menu.MenuItemDetailResult;
import com.mealgo.restaurantservice.application.port.in.menu.RestaurantMenuResult;
import com.mealgo.restaurantservice.application.port.out.RestaurantMenuQueryPort;
import com.mealgo.restaurantservice.domain.enums.*;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.*;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantMenuQueryAdapter implements RestaurantMenuQueryPort {
    private final RestaurantJpaRepository restaurants;
    private final MenuCategoryJpaRepository categories;
    private final MenuItemJpaRepository items;
    private final OptionGroupJpaRepository optionGroups;

    @Override
    public Optional<RestaurantMenuResult> findMenu(UUID restaurantId) {
        return restaurants.findById(restaurantId).map(restaurant -> {
            boolean open = restaurant.getStatus() == RestaurantStatus.OPENING;
            var summary = new RestaurantMenuResult.RestaurantSummary(
                    restaurant.getId(), restaurant.getName(), restaurant.getAddress().getAddressLine(),
                    open, open ? null : "RESTAURANT_CLOSED");
            var itemsWithOptions = new HashSet<>(items.findItemIdsWithActiveOptions(restaurantId));
            var menu = categories.findCustomerMenu(restaurantId).stream()
                    .map(category -> new RestaurantMenuResult.Category(
                            category.getId(), category.getName(), category.getDisplayOrder(),
                            category.getItems().stream()
                                    .filter(item -> item.getStatus() != MenuItemStatus.INACTIVE)
                                    .map(item -> new RestaurantMenuResult.Item(
                                            item.getId(), item.getName(), item.getDescription(),
                                            item.getBasePrice(), item.getImageUrl(),
                                            item.getStatus() == MenuItemStatus.ACTIVE,
                                            itemsWithOptions.contains(item.getId())))
                                    .toList()))
                    .toList();
            return new RestaurantMenuResult(summary, menu);
        });
    }

    @Override
    public Optional<MenuItemDetailResult> findMenuItem(UUID restaurantId, UUID menuItemId) {
        return items.findCustomerItem(restaurantId, menuItemId).map(item -> {
            var groupIds = item.getOptionGroups().stream()
                    .map(MenuItemOptionGroupJpaEntity::getOptionGroupId).toList();
            Map<UUID, OptionGroupJpaEntity> definitions = groupIds.isEmpty() ? Map.of()
                    : optionGroups.findByRestaurantIdAndIdIn(restaurantId, groupIds).stream()
                    .collect(Collectors.toMap(OptionGroupJpaEntity::getId, Function.identity()));

            var groups = new ArrayList<MenuItemDetailResult.OptionGroup>();
            boolean configurationAvailable = true;
            for (var assignment : item.getOptionGroups()) {
                var definition = definitions.get(assignment.getOptionGroupId());
                if (definition == null || definition.getStatus() != OptionGroupStatus.ACTIVE) {
                    // A required group cannot silently disappear and make a dish orderable.
                    if (assignment.getMinSelect() > 0) {
                        configurationAvailable = false;
                    }
                    continue;
                }
                var group = toOptionGroup(assignment, definition);
                long availableCount = group.options().stream()
                        .filter(MenuItemDetailResult.Option::available).count();
                if (availableCount < group.minSelections()) {
                    configurationAvailable = false;
                }
                groups.add(group);
            }

            boolean available = item.getStatus() == MenuItemStatus.ACTIVE
                    && item.getCategory().getRestaurant().getStatus() == RestaurantStatus.OPENING
                    && configurationAvailable;
            return new MenuItemDetailResult(item.getId(), restaurantId, item.getName(),
                    item.getDescription(), item.getBasePrice(), "VND", item.getImageUrl(),
                    available, List.copyOf(groups));
        });
    }

    private MenuItemDetailResult.OptionGroup toOptionGroup(
            MenuItemOptionGroupJpaEntity assignment, OptionGroupJpaEntity definition) {
        var prices = assignment.getOptionPrices().stream()
                .collect(Collectors.toMap(MenuItemOptionPriceJpaEntity::getOptionId,
                        MenuItemOptionPriceJpaEntity::getAdditionalPrice));
        var options = definition.getOptions().stream()
                .filter(option -> option.getStatus() != OptionStatus.INACTIVE)
                // Only options explicitly configured for this dish have a price.
                .filter(option -> prices.containsKey(option.getId()))
                .map(option -> new MenuItemDetailResult.Option(
                        option.getId(), option.getName(), prices.get(option.getId()),
                        option.getStatus() == OptionStatus.ACTIVE, option.getDisplayOrder()))
                .toList();
        return new MenuItemDetailResult.OptionGroup(definition.getId(), definition.getName(),
                assignment.getMinSelect(), assignment.getMaxSelect(),
                assignment.getDisplayOrder(), options);
    }
}
