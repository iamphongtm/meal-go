package com.mealgo.restaurantservice.application.port.out;

import com.mealgo.restaurantservice.domain.model.menuitem.MenuItem;

import java.util.UUID;

public interface MenuItemRepository {
    int getMaxDisplayOrder(UUID categoryId);

    void save(MenuItem menuItem);
}
