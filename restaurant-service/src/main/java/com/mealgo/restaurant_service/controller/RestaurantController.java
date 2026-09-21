package com.mealgo.restaurant_service.controller;

import com.mealgo.restaurant_service.dto.request.RestaurantCreationRequest;
import com.mealgo.restaurant_service.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<Void> init(@Valid @RequestBody RestaurantCreationRequest request) {
        String ownerId = "123"; //TODO get ownerId with authentication
        restaurantService.init(request, ownerId);
        return ResponseEntity.noContent().build();
    }
}
