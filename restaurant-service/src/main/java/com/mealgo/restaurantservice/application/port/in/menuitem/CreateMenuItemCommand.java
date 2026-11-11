package com.mealgo.restaurantservice.application.port.in.menuitem;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateMenuItemCommand(
        UUID restaurantId,
        UUID ownerId,
        UUID categoryId,
        String name,
        String description,
        BigDecimal basePrice,
        String imageUrl,
        List<OptionGroupCommand> optionGroups
) {
    public CreateMenuItemCommand {
        if (optionGroups == null) {
            optionGroups = List.of();
        } else {
            optionGroups = List.copyOf(optionGroups);
        }
    }

    public record OptionGroupCommand(
            UUID optionGroupId,
            Integer minSelect,
            Integer maxSelect,
            List<OptionPriceCommand> optionPrices
    ) {
        public OptionGroupCommand {
            if (optionPrices == null) {
                optionPrices = List.of();
            } else {
                optionPrices = List.copyOf(optionPrices);
            }
        }
    }

    public record OptionPriceCommand(UUID optionId, BigDecimal additionalPrice) {
    }
}
