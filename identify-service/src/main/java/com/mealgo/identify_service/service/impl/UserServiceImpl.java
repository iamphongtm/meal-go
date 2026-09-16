package com.mealgo.identify_service.service.impl;

import com.mealgo.identify_service.domain.enums.UserRole;
import com.mealgo.identify_service.domain.enums.UserStatus;
import com.mealgo.identify_service.domain.model.User;
import com.mealgo.identify_service.domain.repository.UserRepository;
import com.mealgo.identify_service.dto.request.UserCreationRequest;
import com.mealgo.identify_service.exception.BusinessException;
import com.mealgo.identify_service.exception.IdentifyErrorCode;
import com.mealgo.identify_service.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j(topic = "USER-SERVICE")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;

    @Override
    public String createUser(UserCreationRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);

        if (user != null) {
            throw new BusinessException(IdentifyErrorCode.EMAIL_ALREADY_EXISTS, request.email());
        }

        user = userRepository.save(User.builder()
                .email(request.email())
                .passwordHash(request.password())
                .role(UserRole.RESTAURANT)
                .status(UserStatus.ACTIVE)
                .build());
        return user.getUserID().toString();
    }
}
