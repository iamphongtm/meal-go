package com.mealgo.restaurant_service.domain.repository;

import com.mealgo.restaurant_service.domain.model.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, UUID> {
    boolean existsByRestaurantIdAndNameIgnoreCaseAndDeletedAtIsNull(UUID restaurantId, String name);

    @Query("""
            SELECT MAX (c.displayOrder)
            FROM MenuCategory c
            WHERE c.restaurant.id = :restaurantId
              AND c.deletedAt IS null
            """)
    Integer getMaxDisplayOrderByRestaurantId(UUID restaurantId);
}
