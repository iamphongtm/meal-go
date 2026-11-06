package com.mealgo.restaurantservice.infrastructure.persistence.repository;

import com.mealgo.restaurantservice.infrastructure.persistence.entity.MenuItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface MenuItemJpaRepository extends JpaRepository<MenuItemJpaEntity, UUID> {
    java.util.List<MenuItemJpaEntity> findByCategoryRestaurantIdAndIdIn(
            UUID restaurantId, java.util.Collection<UUID> ids);

    @Query("""
            select distinct item from MenuItemJpaEntity item
            join fetch item.category category
            join fetch category.restaurant
            left join fetch item.optionGroups
            where item.id = :itemId and category.restaurant.id = :restaurantId
              and category.status = com.mealgo.restaurantservice.domain.enums.CategoryStatus.ACTIVE
              and item.status <> com.mealgo.restaurantservice.domain.enums.MenuItemStatus.INACTIVE
            """)
    java.util.Optional<MenuItemJpaEntity> findCustomerItem(
            @Param("restaurantId") UUID restaurantId, @Param("itemId") UUID itemId);

    @Query("""
            select distinct g.menuItem.id from MenuItemOptionGroupJpaEntity g
            join OptionGroupJpaEntity og on og.id = g.optionGroupId
            where g.menuItem.category.restaurant.id = :restaurantId
              and og.restaurant.id = :restaurantId
              and og.status = com.mealgo.restaurantservice.domain.enums.OptionGroupStatus.ACTIVE
            """)
    java.util.List<UUID> findItemIdsWithActiveOptions(@Param("restaurantId") UUID restaurantId);

    @Query("""
            select coalesce(max(item.displayOrder), 0)
            from MenuItemJpaEntity item
            where item.category.id = :categoryId
            """)
    int getMaxDisplayOrderByCategoryId(@Param("categoryId") UUID categoryId);
}
