package com.mealgo.restaurant_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OptionCreationRequest(
        @NotBlank(message = "Option name must not be blank")
        @Size(max = 100, message = "Option name must not exceed {max} characters")
        String name,

        @NotNull(message = "Additional price is required")
        @DecimalMin(value = "0.0", message = "Additional price must not be negative")
        BigDecimal additionalPrice
) {
}
