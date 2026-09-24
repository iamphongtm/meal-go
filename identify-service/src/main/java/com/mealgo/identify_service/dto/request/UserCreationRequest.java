package com.mealgo.identify_service.dto.request;

import com.mealgo.identify_service.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreationRequest(
        @NotBlank(message = "{user.email.required}")
        @Email(message = "{user.email.invalid}")
        @Size(max = 255, message = "{user.email.size}")
        String email,

        @NotBlank(message = "{user.password.required}")
        @Size(min = 8, max = 100, message = "{user.password.size}")
        String password,

        @NotNull(message = "{user.role.required}")
        UserRole role
) {
}
