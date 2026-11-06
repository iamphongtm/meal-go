package com.mealgo.restaurantservice.application.port.in.optiongroup;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OptionGroupItems(UUID optionGroupId, String name, List<OptionView> options,
                               List<ItemConfiguration> items) {
    public record OptionView(UUID optionId, String name) {}
    public record ItemConfiguration(UUID menuItemId, Integer minSelect, Integer maxSelect,
                                    List<OptionPrice> optionPrices) {}
    public record OptionPrice(UUID optionId, BigDecimal additionalPrice) {}
}
