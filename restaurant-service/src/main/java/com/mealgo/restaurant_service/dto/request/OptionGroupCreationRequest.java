package com.mealgo.restaurant_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OptionGroupCreationRequest(

        @NotBlank(message = "Option group name must not be blank")
        @Size(max = 100)
        String name, //TODO update message response

        @NotNull
        @Min(0)
        Integer minSelect,

        @NotNull
        @Min(1)
        Integer maxSelect

) {
}
