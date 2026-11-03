package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.application.port.in.menu.GetRestaurantMenuUseCase;
import com.mealgo.restaurantservice.application.port.in.menu.MenuItemDetailResult;
import com.mealgo.restaurantservice.application.port.in.menu.RestaurantMenuResult;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/restaurants/{restaurantId}")
@RequiredArgsConstructor
public class RestaurantMenuController {
    private final GetRestaurantMenuUseCase useCase;

    @Operation(summary = "Get a restaurant's customer-facing menu",
            description = "Includes active categories and visible items. Closed restaurants remain viewable.")
    @GetMapping("/menu")
    public RestaurantMenuResult getMenu(@PathVariable UUID restaurantId) {
        return useCase.getMenu(restaurantId);
    }

    @Operation(summary = "Get a menu item with its configured option groups and prices",
            description = "Inactive items are hidden. Sold-out items remain visible but unavailable.")
    @GetMapping("/menu-items/{menuItemId}")
    public MenuItemDetailResult getMenuItem(@PathVariable UUID restaurantId,
                                           @PathVariable UUID menuItemId) {
        return useCase.getMenuItem(restaurantId, menuItemId);
    }
}
