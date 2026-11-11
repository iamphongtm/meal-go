package com.mealgo.restaurantservice.infrastructure.persistence.mapper;

import com.mealgo.restaurantservice.domain.model.category.MenuCategory;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.MenuCategoryJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.RestaurantJpaEntity;

public final class MenuCategoryPersistenceMapper {
    private MenuCategoryPersistenceMapper() {
    }

    public static MenuCategoryJpaEntity toNewEntity(
            MenuCategory menuCategory,
            RestaurantJpaEntity restaurantReference
    ) {
        MenuCategoryJpaEntity entity = new MenuCategoryJpaEntity();
        entity.setId(menuCategory.getId().value());
        entity.setName(menuCategory.getName());
        entity.setDisplayOrder(menuCategory.getDisplayOrder());
        entity.setStatus(menuCategory.getStatus());
        entity.setRestaurant(restaurantReference);
        return entity;
    }
}
