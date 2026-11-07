package com.mealgo.restaurantservice.infrastructure.persistence.repository;

import com.mealgo.restaurantservice.infrastructure.persistence.entity.MenuItemOptionGroupJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface MenuItemOptionGroupJpaRepository extends JpaRepository<MenuItemOptionGroupJpaEntity, UUID> {
    @Query("""
            select distinct a from MenuItemOptionGroupJpaEntity a
            join fetch a.menuItem i
            left join fetch a.optionPrices
            where i.category.restaurant.id = :restaurantId
              and i.category.status = com.mealgo.restaurantservice.domain.enums.CategoryStatus.ACTIVE
              and i.status <> com.mealgo.restaurantservice.domain.enums.MenuItemStatus.INACTIVE
            order by a.displayOrder, a.id
            """)
    List<MenuItemOptionGroupJpaEntity> findCustomerAssignmentsWithPrices(@Param("restaurantId") UUID restaurantId);

    List<MenuItemOptionGroupJpaEntity> findByMenuItemCategoryRestaurantId(UUID restaurantId);
    List<MenuItemOptionGroupJpaEntity> findByMenuItemCategoryRestaurantIdAndOptionGroupId(
            UUID restaurantId, UUID optionGroupId);
}
