package com.mealgo.restaurantservice.domain.model.optiongroup;

import com.mealgo.restaurantservice.domain.enums.OptionStatus;
import com.mealgo.restaurantservice.domain.shared.Entity;

import java.util.UUID;

public final class Option extends Entity<UUID> {
    private final String name;
    private final OptionStatus status;
    private final Integer displayOrder;

    public Option(UUID id, String name, OptionStatus status, Integer displayOrder) {
        super(id);
        this.name = name;
        this.status = status;
        this.displayOrder = displayOrder;
    }

    public static Option create(String name, int displayOrder) {
        return new Option(UUID.randomUUID(), name, OptionStatus.ACTIVE, displayOrder);
    }

    public String getName() { return name; }
    public OptionStatus getStatus() { return status; }
    public Integer getDisplayOrder() { return displayOrder; }
}
