package com.mealgo.restaurantservice.application.port.in.menu;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ValidateMenuResult(
        boolean valid,
        UUID restaurantId,
        String currency,
        List<Item> items,
        List<Issue> issues
) {
    public record Item(
            UUID lineId,
            UUID menuItemId,
            String name,
            int quantity,
            BigDecimal basePrice,
            List<Option> selectedOptions,
            BigDecimal unitPrice
    ) {
    }

    public record Option(
            UUID optionGroupId,
            UUID optionId,
            String name,
            BigDecimal additionalPrice
    ) {
    }

    public record Issue(
            UUID lineId,
            UUID menuItemId,
            String code,
            String message
    ) {
    }
}