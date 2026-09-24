package com.mealgo.restaurant_service.domain.repository;

import com.mealgo.restaurant_service.domain.model.OptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface OptionGroupRepository extends JpaRepository<OptionGroup, UUID> {
    boolean existsByMenuItemIdAndNameIgnoreCaseAndDeletedAtIsNull(UUID menuItemId, String name);

    @Query("""
            SELECT MAX(o.displayOrder)
            FROM OptionGroup o
            WHERE o.menuItem.id = :menuItemId
              AND o.deletedAt IS NULL
            """)
    Integer getMaxDisplayOrderByMenuItemId(UUID menuItemId);
}
