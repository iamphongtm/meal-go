package com.mealgo.identify_service.dto.response;

import com.mealgo.identify_service.domain.enums.UserRole;
import com.mealgo.identify_service.domain.enums.UserStatus;
import com.mealgo.identify_service.domain.model.User;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        UserRole role,
        UserStatus status
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getUserID(), user.getEmail(), user.getRole(), user.getStatus());
    }
}
