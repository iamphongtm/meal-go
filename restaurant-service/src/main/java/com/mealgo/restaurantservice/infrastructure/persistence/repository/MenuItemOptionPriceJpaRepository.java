package com.mealgo.restaurantservice.infrastructure.persistence.repository;

import com.mealgo.restaurantservice.infrastructure.persistence.entity.MenuItemOptionPriceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MenuItemOptionPriceJpaRepository extends JpaRepository<MenuItemOptionPriceJpaEntity, UUID> {
    List<MenuItemOptionPriceJpaEntity> findByMenuItemOptionGroupId(UUID groupLinkId);
}
