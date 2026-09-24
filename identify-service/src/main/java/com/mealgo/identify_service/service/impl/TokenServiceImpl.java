package com.mealgo.identify_service.service.impl;

import com.mealgo.identify_service.config.JwtProperties;
import com.mealgo.identify_service.domain.model.RefreshToken;
import com.mealgo.identify_service.domain.model.User;
import com.mealgo.identify_service.domain.repository.RefreshTokenRepository;
import com.mealgo.identify_service.dto.response.TokenResponse;
import com.mealgo.identify_service.service.TokenService;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RSAKey rsaKey;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public TokenResponse issueTokenPair(User user, UUID familyId) {
        Instant issuedAt = Instant.now();
        Instant accessExpiresAt = issuedAt.plus(properties.accessTokenTtl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(user.getUserID().toString())
                .audience(java.util.List.of(properties.audience()))
                .issuedAt(issuedAt)
                .expiresAt(accessExpiresAt)
                .id(UUID.randomUUID().toString())
                .claim("role", user.getRole().name())
                .build();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).keyId(rsaKey.getKeyID()).build(), claims)).getTokenValue();

        String refreshToken = generateRefreshToken();
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(hash(refreshToken))
                .familyId(familyId)
                .createdAt(issuedAt)
                .expiresAt(issuedAt.plus(properties.refreshTokenTtl()))
                .build());

        return new TokenResponse(
                accessToken,
                properties.accessTokenTtl().toSeconds(),
                refreshToken,
                properties.refreshTokenTtl().toSeconds());
    }

    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", ex);
        }
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
