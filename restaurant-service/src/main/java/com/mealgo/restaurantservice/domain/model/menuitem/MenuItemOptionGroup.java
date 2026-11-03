package com.mealgo.restaurantservice.domain.model.menuitem;

import com.mealgo.restaurantservice.domain.model.optiongroup.OptionGroupId;
import com.mealgo.restaurantservice.domain.shared.Entity;

import java.util.List;
import java.util.UUID;

public final class MenuItemOptionGroup extends Entity<UUID> {
    private final OptionGroupId optionGroupId;
    private final Integer minSelect;
    private final Integer maxSelect;
    private final Integer displayOrder;
    private final List<MenuItemOptionPrice> optionPrices;

    public MenuItemOptionGroup(UUID id, OptionGroupId optionGroupId, Integer minSelect,
                               Integer maxSelect, Integer displayOrder, List<MenuItemOptionPrice> optionPrices) {
        super(id);
        this.optionGroupId = optionGroupId;
        this.minSelect = minSelect;
        this.maxSelect = maxSelect;
        this.displayOrder = displayOrder;
        this.optionPrices = List.copyOf(optionPrices);
    }

    public OptionGroupId getOptionGroupId() { return optionGroupId; }
    public Integer getMinSelect() { return minSelect; }
    public Integer getMaxSelect() { return maxSelect; }
    public Integer getDisplayOrder() { return displayOrder; }
    public List<MenuItemOptionPrice> getOptionPrices() { return optionPrices; }
}
