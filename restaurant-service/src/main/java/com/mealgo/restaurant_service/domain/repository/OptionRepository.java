package com.mealgo.restaurant_service.domain.repository;

import com.mealgo.restaurant_service.domain.model.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface OptionRepository extends JpaRepository<Option, UUID> {
    boolean existsByOptionGroupIdAndNameIgnoreCaseAndDeletedAtIsNull(UUID optionGroupId, String name);

    @Query("""
            SELECT MAX(o.displayOrder)
            FROM Option o
            WHERE o.optionGroup.id = :optionGroupId
              AND o.deletedAt IS NULL
            """)
    Integer getMaxDisplayOrderByOptionGroupId(UUID optionGroupId);
}
