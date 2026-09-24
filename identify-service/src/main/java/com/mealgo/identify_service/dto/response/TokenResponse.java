package com.mealgo.identify_service.dto.response;

public record TokenResponse(
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        long refreshTokenExpiresIn
) {
}
