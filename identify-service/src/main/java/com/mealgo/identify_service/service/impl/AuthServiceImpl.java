package com.mealgo.identify_service.service.impl;

import com.mealgo.identify_service.domain.enums.UserStatus;
import com.mealgo.identify_service.domain.model.RefreshToken;
import com.mealgo.identify_service.domain.model.User;
import com.mealgo.identify_service.domain.repository.RefreshTokenRepository;
import com.mealgo.identify_service.domain.repository.UserRepository;
import com.mealgo.identify_service.dto.request.LoginRequest;
import com.mealgo.identify_service.dto.response.TokenResponse;
import com.mealgo.identify_service.exception.BusinessException;
import com.mealgo.identify_service.exception.ErrorCode;
import com.mealgo.identify_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenServiceImpl tokenService;

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        assertActive(user);
        return tokenService.issueTokenPair(user, UUID.randomUUID());
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public TokenResponse refresh(String rawRefreshToken) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenService.hash(rawRefreshToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        Instant now = Instant.now();
        if (token.getUsedAt() != null || token.getRevokedAt() != null) {
            refreshTokenRepository.revokeFamily(token.getFamilyId(), now);
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_REUSED);
        }
        if (!token.getExpiresAt().isAfter(now)) {
            token.setRevokedAt(now);
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = token.getUser();
        assertActive(user);
        token.setUsedAt(now);
        TokenResponse response = tokenService.issueTokenPair(user, token.getFamilyId());
        refreshTokenRepository.flush();
        return response;
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHash(tokenService.hash(rawRefreshToken))
                .ifPresent(token -> {
                    if (token.getRevokedAt() == null) {
                        token.setRevokedAt(Instant.now());
                    }
                });
    }

    private void assertActive(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
