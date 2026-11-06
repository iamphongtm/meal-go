package com.mealgo.restaurantservice.infrastructure.persistence.adapter;

import com.mealgo.restaurantservice.application.port.in.optiongroup.OptionGroupItems;
import com.mealgo.restaurantservice.application.port.out.OptionGroupItemsRepository;
import com.mealgo.restaurantservice.domain.enums.OptionGroupStatus;
import com.mealgo.restaurantservice.domain.enums.OptionStatus;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.*;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import static com.mealgo.restaurantservice.domain.shared.ErrorCode.*;

@Repository
@RequiredArgsConstructor
public class OptionGroupItemsAdapter implements OptionGroupItemsRepository {
    private final OptionGroupJpaRepository groups;
    private final MenuItemJpaRepository menuItems;
    private final MenuItemOptionGroupJpaRepository links;
    private final MenuItemOptionPriceJpaRepository prices;

    @Override
    public List<OptionGroupItems> findAll(UUID restaurantId) {
        var allLinks = links.findByMenuItemCategoryRestaurantId(restaurantId);
        return groups.findByRestaurantIdOrderByNameAsc(restaurantId).stream()
                .filter(group -> group.getStatus() == OptionGroupStatus.ACTIVE)
                .map(group -> new OptionGroupItems(group.getId(), group.getName(),
                        group.getOptions().stream().filter(option -> option.getStatus() == OptionStatus.ACTIVE)
                                .map(option -> new OptionGroupItems.OptionView(option.getId(), option.getName())).toList(),
                        allLinks.stream().filter(link -> link.getOptionGroupId().equals(group.getId()))
                                .map(link -> new OptionGroupItems.ItemConfiguration(link.getMenuItem().getId(),
                                        link.getMinSelect(), link.getMaxSelect(), link.getOptionPrices().stream()
                                        .map(price -> new OptionGroupItems.OptionPrice(price.getOptionId(), price.getAdditionalPrice()))
                                        .toList())).toList())).toList();
    }

    @Override
    public void replaceItems(UUID restaurantId, UUID groupId, List<OptionGroupItems.ItemConfiguration> requested) {
        var group = groups.findByIdAndRestaurantId(groupId, restaurantId)
                .orElseThrow(() -> new BusinessException(OPTION_GROUP_NOT_FOUND, groupId));
        var activeOptions = group.getOptions().stream().filter(option -> option.getStatus() == OptionStatus.ACTIVE).toList();
        if (group.getStatus() != OptionGroupStatus.ACTIVE || activeOptions.isEmpty()) {
            throw new BusinessException(OPTION_GROUP_CONFIGURATION_INVALID, groupId);
        }
        var optionIds = activeOptions.stream().map(OptionJpaEntity::getId).collect(Collectors.toSet());
        var itemIds = new HashSet<UUID>();
        for (var item : requested) {
            if (!itemIds.add(item.menuItemId())) throw new BusinessException(MENU_ITEM_CONFLICT, item.menuItemId());
            if (item.minSelect() < 0 || item.maxSelect() < 1 || item.minSelect() > item.maxSelect()
                    || item.maxSelect() > activeOptions.size()) {
                throw new BusinessException(OPTION_GROUP_CONFIGURATION_INVALID, groupId);
            }
            var seen = new HashSet<UUID>();
            for (var price : item.optionPrices()) {
                if (!optionIds.contains(price.optionId())) throw new BusinessException(OPTION_NOT_FOUND, price.optionId());
                if (!seen.add(price.optionId()) || price.additionalPrice().signum() < 0) {
                    throw new BusinessException(OPTION_CONFLICT, price.optionId());
                }
            }
        }
        var itemsById = (itemIds.isEmpty() ? List.<MenuItemJpaEntity>of()
                : menuItems.findByCategoryRestaurantIdAndIdIn(restaurantId, itemIds)).stream()
                .collect(Collectors.toMap(MenuItemJpaEntity::getId, Function.identity()));
        for (var id : itemIds) {
            if (!itemsById.containsKey(id)) throw new BusinessException(MENU_ITEM_NOT_FOUND, id);
        }
        var existing = links.findByMenuItemCategoryRestaurantIdAndOptionGroupId(restaurantId, groupId);
        var byItem = existing.stream().collect(Collectors.toMap(link -> link.getMenuItem().getId(), Function.identity()));
        for (var link : existing) {
            if (!itemIds.contains(link.getMenuItem().getId())) {
                prices.deleteAll(prices.findByMenuItemOptionGroupId(link.getId()));
                prices.flush();
                links.delete(link);
            }
        }
        links.flush();
        for (var configuration : requested) {
            var link = byItem.get(configuration.menuItemId());
            if (link == null) {
                link = new MenuItemOptionGroupJpaEntity();
                link.setId(UUID.randomUUID());
                var item = itemsById.get(configuration.menuItemId());
                link.setMenuItem(item);
                link.setOptionGroupId(groupId);
                link.setDisplayOrder(item.getOptionGroups().stream()
                        .mapToInt(MenuItemOptionGroupJpaEntity::getDisplayOrder).max().orElse(0) + 1);
            }
            link.setMinSelect(configuration.minSelect());
            link.setMaxSelect(configuration.maxSelect());
            var saved = links.save(link);
            var oldPrices = prices.findByMenuItemOptionGroupId(saved.getId()).stream()
                    .collect(Collectors.toMap(MenuItemOptionPriceJpaEntity::getOptionId, Function.identity()));
            var requestedPrices = configuration.optionPrices().stream().collect(Collectors.toMap(
                    OptionGroupItems.OptionPrice::optionId, OptionGroupItems.OptionPrice::additionalPrice));
            for (var option : activeOptions) {
                var price = oldPrices.get(option.getId());
                if (price == null) {
                    price = new MenuItemOptionPriceJpaEntity();
                    price.setId(UUID.randomUUID());
                    price.setMenuItemOptionGroup(saved);
                    price.setOptionId(option.getId());
                }
                price.setAdditionalPrice(requestedPrices.getOrDefault(option.getId(), BigDecimal.ZERO));
                prices.save(price);
            }
        }
    }
}
