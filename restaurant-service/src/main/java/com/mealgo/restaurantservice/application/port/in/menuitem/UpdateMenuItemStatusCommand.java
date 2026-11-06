package com.mealgo.restaurantservice.application.port.in.menuitem;

import com.mealgo.restaurantservice.domain.enums.MenuItemStatus;
import java.util.UUID;

public record UpdateMenuItemStatusCommand(UUID restaurantId, UUID ownerId, UUID menuItemId, MenuItemStatus status) {}
