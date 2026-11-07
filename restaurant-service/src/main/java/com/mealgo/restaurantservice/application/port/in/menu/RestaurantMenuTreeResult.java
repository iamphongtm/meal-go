package com.mealgo.restaurantservice.application.port.in.menu;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RestaurantMenuTreeResult(RestaurantSummary restaurant, List<CategoryNode> categories) {
    public record RestaurantSummary(UUID restaurantId, String name, String addressLine,
                                    boolean acceptingOrders, String unavailableReason) {}

    public record CategoryNode(UUID categoryId, String name, Integer displayOrder, List<MenuItemNode> items) {}

    public record MenuItemNode(UUID menuItemId, String name, String description, BigDecimal basePrice,
                               String currency, String imageUrl, boolean available,
                               List<OptionGroupNode> optionGroups) {}

    public record OptionGroupNode(UUID optionGroupId, String name, int minSelections,
                                  int maxSelections, Integer displayOrder, List<OptionNode> options) {}

    public record OptionNode(UUID optionId, String name, BigDecimal additionalPrice,
                             boolean available, Integer displayOrder) {}
}
