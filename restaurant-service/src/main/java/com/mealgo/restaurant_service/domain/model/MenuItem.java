package com.mealgo.restaurant_service.domain.model;

import com.mealgo.restaurant_service.domain.enums.MenuItemStatus;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class MenuItem {
    UUID id;
    String name;
    String description;

    BigDecimal basePrice;
    String imageUrl;

    MenuItemStatus status;
    Integer displayOrder;

    List<OptionGroup> optionGroups;
}
