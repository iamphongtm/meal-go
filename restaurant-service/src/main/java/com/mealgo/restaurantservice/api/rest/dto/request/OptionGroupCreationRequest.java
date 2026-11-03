package com.mealgo.restaurantservice.api.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OptionGroupCreationRequest(

        @NotBlank(message = "{option-group.name.required}")
        @Size(max = 100, message = "{option-group.name.max-length}")
        String name,

        @NotEmpty(message = "{option-group.options.required}")
        List<
                @NotNull(message = "{option-group.options.item.required}")
                @Valid OptionCreationRequest
                > options

) {
    public record OptionCreationRequest(

            @NotBlank(message = "{option.name.required}")
            @Size(max = 255, message = "{option.name.max-length}")
            String name
    ) {
    }
}