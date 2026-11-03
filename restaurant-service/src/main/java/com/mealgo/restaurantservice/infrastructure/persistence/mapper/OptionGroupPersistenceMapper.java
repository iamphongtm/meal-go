package com.mealgo.restaurantservice.infrastructure.persistence.mapper;

import com.mealgo.restaurantservice.domain.model.optiongroup.OptionGroup;
import com.mealgo.restaurantservice.domain.model.optiongroup.Option;
import com.mealgo.restaurantservice.domain.model.optiongroup.OptionGroupId;
import com.mealgo.restaurantservice.domain.model.restaurant.RestaurantId;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.OptionGroupJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.OptionJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.RestaurantJpaEntity;

import java.util.ArrayList;

public final class OptionGroupPersistenceMapper {
    private OptionGroupPersistenceMapper() {
    }

    public static OptionGroup toDomain(OptionGroupJpaEntity entity) {
        var options = entity.getOptions().stream()
                .map(option -> new Option(option.getId(), option.getName(), option.getStatus(), option.getDisplayOrder()))
                .toList();
        return new OptionGroup(OptionGroupId.of(entity.getId()), RestaurantId.of(entity.getRestaurant().getId()),
                entity.getName(), entity.getStatus(), options);
    }

    public static OptionGroupJpaEntity toNewEntity(
            OptionGroup optionGroup,
            RestaurantJpaEntity restaurantReference
    ) {
        var entity = new OptionGroupJpaEntity();
        entity.setId(optionGroup.getId().value());
        entity.setRestaurant(restaurantReference);
        entity.setName(optionGroup.getName());
        entity.setStatus(optionGroup.getStatus());

        var options = new ArrayList<OptionJpaEntity>();
        for (var option : optionGroup.getOptions()) {
            var optionEntity = new OptionJpaEntity();
            optionEntity.setId(option.getId());
            optionEntity.setName(option.getName());
            optionEntity.setStatus(option.getStatus());
            optionEntity.setDisplayOrder(option.getDisplayOrder());
            optionEntity.setOptionGroup(entity);
            options.add(optionEntity);
        }
        entity.setOptions(options);
        return entity;
    }
}
