package com.mealgo.restaurant_service.domain.model;

import com.mealgo.restaurant_service.domain.enums.OptionStatus;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Option {
    UUID id;
    String name;

    BigDecimal additionalPrice;
    OptionStatus status;
    Integer displayOrder;
}
