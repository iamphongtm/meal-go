package com.mealgo.restaurantservice.api.rest.dto.request;

import jakarta.validation.constraints.*;

public record RegisterRestaurantRequest(
        @NotBlank(message = "{restaurant.name.required}")
        @Size(max = 150, message = "{restaurant.name.max-length}")
        String name,

        @NotBlank(message = "{restaurant.phone.required}")
        @Pattern(regexp = "^0[35789][0-9]{8}$", message = "{restaurant.phone.invalid}")
        String phone,

        @NotNull(message = "{restaurant.province-code.required}")
        @Positive(message = "{restaurant.province-code.positive}")
        Integer provinceCode,

        @NotNull(message = "{restaurant.ward-code.required}")
        @Positive(message = "{restaurant.ward-code.positive}")
        Integer wardCode,

        @NotBlank(message = "{restaurant.address-line.required}")
        @Size(max = 255, message = "{restaurant.address-line.max-length}")
        String addressLine,

        @NotNull(message = "{restaurant.latitude.required}")
        @DecimalMin(value = "-90.0", message = "{restaurant.latitude.min}")
        @DecimalMax(value = "90.0", message = "{restaurant.latitude.max}")
        Double latitude,

        @NotNull(message = "{restaurant.longitude.required}")
        @DecimalMin(value = "-180.0", message = "{restaurant.longitude.min}")
        @DecimalMax(value = "180.0", message = "{restaurant.longitude.max}")
        Double longitude
) {
}
