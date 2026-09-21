package com.mealgo.restaurant_service.domain.model;

import com.mealgo.restaurant_service.domain.enums.OptionGroupStatus;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class OptionGroup {

    UUID id;
    String name;

    Integer minSelect;
    Integer maxSelect;

    OptionGroupStatus status;

    List<Option> options;
}
