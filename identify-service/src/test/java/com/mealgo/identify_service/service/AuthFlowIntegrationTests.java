package com.mealgo.identify_service.service;

import com.mealgo.identify_service.domain.enums.UserRole;
import com.mealgo.identify_service.domain.repository.RefreshTokenRepository;
import com.mealgo.identify_service.domain.repository.UserRepository;
import com.mealgo.identify_service.dto.request.LoginRequest;
import com.mealgo.identify_service.dto.request.UserCreationRequest;
import com.mealgo.identify_service.dto.response.TokenResponse;
import com.mealgo.identify_service.exception.BusinessException;
import com.mealgo.identify_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AuthFlowIntegrationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void cleanDatabase() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void loginIssuesJwtAndRefreshRotatesToken() {
        var user = userService.createUser(new UserCreationRequest(
                "Owner@Example.com", "password123", UserRole.RESTAURANT));

        TokenResponse firstPair = authService.login(new LoginRequest("owner@example.com", "password123"));
        Jwt accessToken = jwtDecoder.decode(firstPair.accessToken());

        assertThat(accessToken.getSubject()).isEqualTo(user.id().toString());
        assertThat(accessToken.getClaimAsString("role")).isEqualTo("RESTAURANT");

        TokenResponse secondPair = authService.refresh(firstPair.refreshToken());
        assertThat(secondPair.refreshToken()).isNotEqualTo(firstPair.refreshToken());

        assertThatThrownBy(() -> authService.refresh(firstPair.refreshToken()))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_REUSED));
    }
}
