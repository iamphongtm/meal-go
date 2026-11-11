package com.mealgo.restaurantservice.api.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ValidateMenuRequest(
        @NotEmpty
        @Size(max = 50)
        List<@NotNull @Valid Item> items
) {
    public record Item(
            @NotNull UUID lineId,
            @NotNull UUID menuItemId,
            @NotNull @Min(1) @Max(99) Integer quantity,
            @NotNull
            @Size(max = 50)
            List<@NotNull UUID> selectedOptionIds
    ) {
    }
}