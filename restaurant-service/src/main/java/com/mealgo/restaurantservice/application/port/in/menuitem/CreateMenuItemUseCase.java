package com.mealgo.restaurantservice.application.port.in.menuitem;

import com.mealgo.restaurantservice.domain.model.menuitem.MenuItemId;

public interface CreateMenuItemUseCase {
    MenuItemId createMenuItem(CreateMenuItemCommand command);
}
