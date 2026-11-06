package com.mealgo.restaurantservice.infrastructure.persistence.mapper;

import com.mealgo.restaurantservice.domain.model.menuitem.MenuItem;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.MenuCategoryJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.MenuItemJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.MenuItemOptionGroupJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.MenuItemOptionPriceJpaEntity;

import java.util.ArrayList;

public final class MenuItemPersistenceMapper {
    private MenuItemPersistenceMapper() {
    }

    public static MenuItem toDomain(MenuItemJpaEntity entity) {
        var groups = entity.getOptionGroups().stream().map(group ->
                new com.mealgo.restaurantservice.domain.model.menuitem.MenuItemOptionGroup(
                        group.getId(), com.mealgo.restaurantservice.domain.model.optiongroup.OptionGroupId.of(group.getOptionGroupId()), group.getMinSelect(), group.getMaxSelect(),
                        group.getDisplayOrder(), group.getOptionPrices().stream().map(price ->
                        new com.mealgo.restaurantservice.domain.model.menuitem.MenuItemOptionPrice(
                                price.getId(), price.getOptionId(), price.getAdditionalPrice())).toList())).toList();
        return new MenuItem(com.mealgo.restaurantservice.domain.model.menuitem.MenuItemId.of(entity.getId()),
                com.mealgo.restaurantservice.domain.model.category.MenuCategoryId.of(entity.getCategory().getId()),
                entity.getName(), entity.getDescription(), entity.getBasePrice(), entity.getImageUrl(),
                entity.getStatus(), entity.getDisplayOrder(), groups);
    }

    public static MenuItemJpaEntity toNewEntity(MenuItem menuItem, MenuCategoryJpaEntity categoryReference) {
        var entity = new MenuItemJpaEntity();
        entity.setId(menuItem.getId().value());
        entity.setCategory(categoryReference);
        entity.setName(menuItem.getName());
        entity.setDescription(menuItem.getDescription());
        entity.setBasePrice(menuItem.getBasePrice());
        entity.setImageUrl(menuItem.getImageUrl());
        entity.setStatus(menuItem.getStatus());
        entity.setDisplayOrder(menuItem.getDisplayOrder());

        var groups = new ArrayList<MenuItemOptionGroupJpaEntity>();
        for (var group : menuItem.getOptionGroups()) {
            var groupEntity = new MenuItemOptionGroupJpaEntity();
            groupEntity.setId(group.getId());
            groupEntity.setMenuItem(entity);
            groupEntity.setOptionGroupId(group.getOptionGroupId().value());
            groupEntity.setMinSelect(group.getMinSelect());
            groupEntity.setMaxSelect(group.getMaxSelect());
            groupEntity.setDisplayOrder(group.getDisplayOrder());

            var prices = new ArrayList<MenuItemOptionPriceJpaEntity>();
            for (var price : group.getOptionPrices()) {
                var priceEntity = new MenuItemOptionPriceJpaEntity();
                priceEntity.setId(price.getId());
                priceEntity.setMenuItemOptionGroup(groupEntity);
                priceEntity.setOptionId(price.getOptionId());
                priceEntity.setAdditionalPrice(price.getAdditionalPrice());
                prices.add(priceEntity);
            }
            groupEntity.setOptionPrices(prices);
            groups.add(groupEntity);
        }
        entity.setOptionGroups(groups);
        return entity;
    }
}
