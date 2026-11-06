package com.mealgo.restaurantservice.api.rest.dto.request;

import com.mealgo.restaurantservice.domain.enums.MenuItemStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateMenuItemStatusRequest(@NotNull MenuItemStatus status) {}
