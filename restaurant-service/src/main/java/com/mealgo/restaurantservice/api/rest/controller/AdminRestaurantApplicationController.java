package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.api.rest.dto.request.ReviewApplicationRequest;
import com.mealgo.restaurantservice.api.rest.dto.response.*;
import com.mealgo.restaurantservice.api.rest.mapper.ApplicationApiMapper;
import com.mealgo.restaurantservice.application.usecase.RestaurantApplicationUseCase;
import com.mealgo.restaurantservice.domain.enums.ApprovalStatus;
import com.mealgo.restaurantservice.infrastructure.config.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/admin/restaurant-applications")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestaurantApplicationController {
    private final RestaurantApplicationUseCase useCase;
    private final ApplicationApiMapper mapper;

    @GetMapping
    public ApplicationPageResponse list(@RequestParam(defaultValue = "PENDING") ApprovalStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return mapper.toResponse(useCase.list(mapper.toQuery(status, page, size)));
    }

    @GetMapping("/{restaurantId}")
    public ApplicationResponse get(@PathVariable UUID restaurantId) {
        return mapper.toResponse(useCase.get(restaurantId));
    }

    @PatchMapping("/{restaurantId}/review")
    public ApplicationResponse review(@PathVariable UUID restaurantId,
            @AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody ReviewApplicationRequest request) {
        return mapper.toResponse(useCase.review(mapper.toCommand(restaurantId, user.userId(), request)));
    }
}
