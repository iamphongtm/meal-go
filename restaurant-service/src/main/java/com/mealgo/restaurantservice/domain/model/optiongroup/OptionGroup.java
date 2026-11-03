package com.mealgo.restaurantservice.domain.model.optiongroup;

import com.mealgo.restaurantservice.domain.enums.OptionGroupStatus;
import com.mealgo.restaurantservice.domain.model.restaurant.RestaurantId;
import com.mealgo.restaurantservice.domain.shared.Entity;
import com.mealgo.restaurantservice.domain.shared.IAggregateRoot;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Getter
public final class OptionGroup extends Entity<OptionGroupId> implements IAggregateRoot {
    private final RestaurantId restaurantId;
    private final String name;
    private final OptionGroupStatus status;
    private final List<Option> options;

    public OptionGroup(OptionGroupId id, RestaurantId restaurantId, String name,
                       OptionGroupStatus status, List<Option> options) {
        super(id);
        this.restaurantId = restaurantId;
        this.name = name;
        this.status = status;
        this.options = List.copyOf(options);
    }

    public static OptionGroup create(RestaurantId restaurantId, String name, List<String> optionNames) {
        List<Option> options = new ArrayList<>();

        for (int i = 0; i < optionNames.size(); i++) {
            options.add(
                    Option.create(optionNames.get(i), i + 1)
            );
        }

        return new OptionGroup(OptionGroupId.newId(), restaurantId, name, OptionGroupStatus.ACTIVE, options);
    }

}
