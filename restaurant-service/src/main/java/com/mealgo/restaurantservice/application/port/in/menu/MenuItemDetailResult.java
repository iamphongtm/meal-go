package com.mealgo.restaurantservice.application.port.in.menu;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MenuItemDetailResult(UUID menuItemId, UUID restaurantId, String name,
                                   String description, BigDecimal basePrice, String currency,
                                   String imageUrl, boolean available, List<OptionGroup> optionGroups) {
    public record OptionGroup(UUID optionGroupId, String name, int minSelections,
                              int maxSelections, Integer displayOrder, List<Option> options) {}

    public record Option(UUID optionId, String name, BigDecimal additionalPrice,
                         boolean available, Integer displayOrder) {}
}
