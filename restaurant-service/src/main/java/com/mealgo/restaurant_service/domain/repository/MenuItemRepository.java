package com.mealgo.restaurant_service.domain.repository;

import com.mealgo.restaurant_service.domain.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
}
