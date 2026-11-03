package com.mealgo.restaurantservice.api.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreationRequest(
        @Schema(description = "Menu category name", example = "Cơm")
        @NotBlank(message = "{menu-category.name.required}")
        @Size(max = 100, message = "{menu-category.name.max-length}")
        String name
) {
}
