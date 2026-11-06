package com.mealgo.restaurantservice.infrastructure.persistence.repository;

import com.mealgo.restaurantservice.infrastructure.persistence.entity.MenuItemOptionGroupJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MenuItemOptionGroupJpaRepository extends JpaRepository<MenuItemOptionGroupJpaEntity, UUID> {
    List<MenuItemOptionGroupJpaEntity> findByMenuItemCategoryRestaurantId(UUID restaurantId);
    List<MenuItemOptionGroupJpaEntity> findByMenuItemCategoryRestaurantIdAndOptionGroupId(
            UUID restaurantId, UUID optionGroupId);
}
