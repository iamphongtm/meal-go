package com.mealgo.identify_service.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "{refresh-token.required}")
        String refreshToken
) {
}
