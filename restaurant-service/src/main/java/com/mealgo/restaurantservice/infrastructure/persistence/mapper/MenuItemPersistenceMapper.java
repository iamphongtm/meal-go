package com.mealgo.restaurantservice.infrastructure.persistence.mapper;

import com.mealgo.restaurantservice.domain.model.menuitem.MenuItem;
import com.mealgo.restaurantservice.domain.model.menuitem.MenuItemOptionGroup;
import com.mealgo.restaurantservice.domain.model.menuitem.MenuItemOptionPrice;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.MenuCategoryJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.MenuItemJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.MenuItemOptionGroupJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.MenuItemOptionPriceJpaEntity;

import java.util.ArrayList;
import java.util.List;

public final class MenuItemPersistenceMapper {
    private MenuItemPersistenceMapper() {
    }

    public static MenuItem toDomain(MenuItemJpaEntity entity) {
        List<MenuItemOptionGroup> groups = entity.getOptionGroups().stream().map(group ->
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
        MenuItemJpaEntity entity = new MenuItemJpaEntity();
        entity.setId(menuItem.getId().value());
        entity.setCategory(categoryReference);
        entity.setName(menuItem.getName());
        entity.setDescription(menuItem.getDescription());
        entity.setBasePrice(menuItem.getBasePrice());
        entity.setImageUrl(menuItem.getImageUrl());
        entity.setStatus(menuItem.getStatus());
        entity.setDisplayOrder(menuItem.getDisplayOrder());

        List<MenuItemOptionGroupJpaEntity> groups = new ArrayList<>();
        for (MenuItemOptionGroup group : menuItem.getOptionGroups()) {
            MenuItemOptionGroupJpaEntity groupEntity = new MenuItemOptionGroupJpaEntity();
            groupEntity.setId(group.getId());
            groupEntity.setMenuItem(entity);
            groupEntity.setOptionGroupId(group.getOptionGroupId().value());
            groupEntity.setMinSelect(group.getMinSelect());
            groupEntity.setMaxSelect(group.getMaxSelect());
            groupEntity.setDisplayOrder(group.getDisplayOrder());

            List<MenuItemOptionPriceJpaEntity> prices = new ArrayList<>();
            for (MenuItemOptionPrice price : group.getOptionPrices()) {
                MenuItemOptionPriceJpaEntity priceEntity = new MenuItemOptionPriceJpaEntity();
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
