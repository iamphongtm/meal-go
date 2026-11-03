package com.mealgo.restaurantservice.application.port.in.optiongroup;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record OptionGroupCreationCommand(
        UUID restaurantId,
        UUID ownerId,
        String name,
        List<OptionCreationCommand> options
) {
    @Builder
    public record OptionCreationCommand(
            String name
    ){
    }
}
