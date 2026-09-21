package com.mealgo.identify_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreationRequest(
        @NotBlank(message = "{user.email.required}")
        String email,

        @NotBlank(message = "{user.password.required}")
        @Size(min = 8, max = 100, message = "{user.password.size}")
        String password
) {
}
