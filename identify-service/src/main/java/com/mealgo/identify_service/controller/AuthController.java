package com.mealgo.identify_service.controller;

import com.mealgo.identify_service.dto.request.LoginRequest;
import com.mealgo.identify_service.dto.request.RefreshTokenRequest;
import com.mealgo.identify_service.dto.request.UserCreationRequest;
import com.mealgo.identify_service.dto.response.TokenResponse;
import com.mealgo.identify_service.dto.response.UserResponse;
import com.mealgo.identify_service.service.AuthService;
import com.mealgo.identify_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @RequestBody @Valid UserCreationRequest request,
            UriComponentsBuilder uriBuilder) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.created(uriBuilder.path("/users/{id}").build(user.id())).body(user);
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
