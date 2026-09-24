package com.mealgo.identify_service.service;

import com.mealgo.identify_service.dto.request.UserCreationRequest;
import com.mealgo.identify_service.dto.response.UserResponse;
import org.springframework.security.oauth2.jwt.Jwt;

public interface UserService {

    UserResponse createUser(UserCreationRequest request);

    UserResponse getCurrentUser(Jwt jwt);
}
