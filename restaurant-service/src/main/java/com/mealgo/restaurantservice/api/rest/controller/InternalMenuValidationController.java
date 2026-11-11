package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.api.rest.dto.request.ValidateMenuRequest;
import com.mealgo.restaurantservice.api.rest.dto.response.ValidateMenuResponse;
import com.mealgo.restaurantservice.api.rest.mapper.MenuValidationApiMapper;
import com.mealgo.restaurantservice.application.port.in.menu.ValidateMenuUseCase;
import com.mealgo.restaurantservice.application.port.in.menu.ValidateMenuQuery;
import com.mealgo.restaurantservice.application.port.in.menu.ValidateMenuResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/restaurants/{restaurantId}/menu")
@RequiredArgsConstructor
public class InternalMenuValidationController {

    private final ValidateMenuUseCase validateMenuUseCase;
    private final MenuValidationApiMapper mapper;

    @PostMapping("/validate")
    public ValidateMenuResponse validate(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody ValidateMenuRequest request
    ) {
        ValidateMenuQuery query = mapper.toQuery(restaurantId, request);
        ValidateMenuResult result = validateMenuUseCase.validate(query);
        return mapper.toResponse(result);
    }
}
