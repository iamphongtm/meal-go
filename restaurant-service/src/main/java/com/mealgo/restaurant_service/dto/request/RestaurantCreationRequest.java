package com.mealgo.restaurant_service.dto.request;

import jakarta.validation.constraints.*;

public record RestaurantCreationRequest(

        @NotBlank(message = "{restaurant.name.required}")
        @Size(
                max = 150,
                message = "{restaurant.name.max-length}"
        )
        String name,

        @NotBlank(message = "{restaurant.phone.required}")
        @Size(
                max = 16,
                message = "{restaurant.phone.max-length}"
        )
        @Pattern(
                regexp = "^\\+[1-9]\\d{7,14}$",
                message = "{restaurant.phone.invalid}"
        )
        String phone,

        @NotNull(message = "{restaurant.province-code.required}")
        @Positive(message = "{restaurant.province-code.positive}")
        Integer provinceCode,

        @NotNull(message = "{restaurant.ward-code.required}")
        @Positive(message = "{restaurant.ward-code.positive}")
        Integer wardCode,

        @NotBlank(message = "{restaurant.address-line.required}")
        @Size(
                max = 300,
                message = "{restaurant.address-line.max-length}"
        )
        String addressLine
) {
}
