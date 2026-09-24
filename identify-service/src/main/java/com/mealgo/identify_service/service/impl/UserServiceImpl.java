package com.mealgo.identify_service.service.impl;

import com.mealgo.identify_service.domain.enums.UserStatus;
import com.mealgo.identify_service.domain.model.User;
import com.mealgo.identify_service.domain.repository.UserRepository;
import com.mealgo.identify_service.dto.request.UserCreationRequest;
import com.mealgo.identify_service.dto.response.UserResponse;
import com.mealgo.identify_service.exception.BusinessException;
import com.mealgo.identify_service.exception.ErrorCode;
import com.mealgo.identify_service.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j(topic = "USER-SERVICE")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(UserCreationRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException(ErrorCode.USER_CONFLICT, request.email());
        }

        try {
            User user = userRepository.saveAndFlush(User.builder()
                    .email(normalizedEmail)
                    .passwordHash(passwordEncoder.encode(request.password()))
                    .role(request.role())
                    .status(UserStatus.ACTIVE)
                    .build());
            return UserResponse.from(user);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(ErrorCode.USER_CONFLICT, request.email());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Jwt jwt) {
        try {
            User user = userRepository.findById(UUID.fromString(jwt.getSubject()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, jwt.getSubject()));
            return UserResponse.from(user);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, jwt.getSubject());
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
