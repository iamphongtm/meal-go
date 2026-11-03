package com.mealgo.restaurantservice.domain.model.menuitem;

import com.mealgo.restaurantservice.domain.shared.Entity;

import java.math.BigDecimal;
import java.util.UUID;

public final class MenuItemOptionPrice extends Entity<UUID> {
    private final UUID optionId;
    private final BigDecimal additionalPrice;

    public MenuItemOptionPrice(UUID id, UUID optionId, BigDecimal additionalPrice) {
        super(id);
        this.optionId = optionId;
        this.additionalPrice = additionalPrice;
    }

    public UUID getOptionId() { return optionId; }
    public BigDecimal getAdditionalPrice() { return additionalPrice; }
}
