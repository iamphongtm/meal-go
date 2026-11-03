package com.mealgo.restaurantservice.application.port.out;

import com.mealgo.restaurantservice.domain.model.optiongroup.OptionGroup;

import java.util.UUID;
import java.util.Optional;

public interface OptionGroupRepository {
    boolean existsByRestaurantIdAndName(UUID restaurantId, String name);

    void save(OptionGroup optionGroup);

    Optional<OptionGroup> findByIdAndRestaurantId(UUID id, UUID restaurantId);
}
