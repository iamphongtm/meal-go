package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.api.rest.dto.request.RegisterRestaurantRequest;
import com.mealgo.restaurantservice.api.rest.dto.response.ApplicationResponse;
import com.mealgo.restaurantservice.api.rest.mapper.ApplicationApiMapper;
import com.mealgo.restaurantservice.application.usecase.RestaurantApplicationUseCase;
import com.mealgo.restaurantservice.infrastructure.config.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/restaurants")
@PreAuthorize("hasAnyRole('CUSTOMER', 'RESTAURANT')")
public class RestaurantApplicationController {
    private final RestaurantApplicationUseCase useCase;
    private final ApplicationApiMapper mapper;

    @GetMapping("/me/application")
    public ApplicationResponse mine(@AuthenticationPrincipal AuthenticatedUser user) {
        return mapper.toResponse(useCase.getMine(user.userId()));
    }

    @PutMapping("/{restaurantId}/application")
    public ApplicationResponse resubmit(@PathVariable UUID restaurantId,
            @AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody RegisterRestaurantRequest request) {
        return mapper.toResponse(useCase.resubmit(mapper.toCommand(restaurantId, user.userId(), request)));
    }
}
