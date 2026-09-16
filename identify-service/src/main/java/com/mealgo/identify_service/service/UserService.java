package com.mealgo.identify_service.service;

import com.mealgo.identify_service.dto.request.UserCreationRequest;

public interface UserService {

    String createUser(UserCreationRequest request);
}
