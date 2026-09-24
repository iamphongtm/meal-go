package com.mealgo.identify_service.service;

import com.mealgo.identify_service.dto.request.LoginRequest;
import com.mealgo.identify_service.dto.response.TokenResponse;

public interface AuthService {

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(String refreshToken);

    void logout(String refreshToken);
}
