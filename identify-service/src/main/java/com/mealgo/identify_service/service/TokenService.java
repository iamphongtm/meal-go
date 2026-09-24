package com.mealgo.identify_service.service;

import com.mealgo.identify_service.domain.model.User;
import com.mealgo.identify_service.dto.response.TokenResponse;

import java.util.UUID;

public interface TokenService {

    TokenResponse issueTokenPair(User user, UUID familyId);
}
