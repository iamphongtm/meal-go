package com.mealgo.identify_service.controller;

import com.mealgo.identify_service.dto.request.UserCreationRequest;
import com.mealgo.identify_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j(topic = "USER-CONTROLLER")
public class UserController {

    private final UserService userService;

    @PostMapping("/")
    public ResponseEntity<String> register(@RequestBody @Valid UserCreationRequest request) {
        log.info("UserCreationRequest : {}", request.email());
        String id = userService.createUser(request);
        log.info("User created successfully : {}", request.email());
        return ResponseEntity
                .ok()
                .body(id);
    }
}
