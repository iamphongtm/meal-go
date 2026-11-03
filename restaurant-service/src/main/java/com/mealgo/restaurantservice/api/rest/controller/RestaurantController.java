package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.application.port.in.restaurant.RegisterRestaurantUseCase;
import com.mealgo.restaurantservice.application.port.in.restaurant.RegisterRestaurantCommand;
import com.mealgo.restaurantservice.api.rest.dto.request.RegisterRestaurantRequest;
import com.mealgo.restaurantservice.api.rest.dto.response.RegisterRestaurantResponse;
import com.mealgo.restaurantservice.infrastructure.config.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
@Slf4j(topic = "RESTAURANT_CONTROLLER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RestaurantController {
    RegisterRestaurantUseCase registerRestaurantUseCase;

    @Operation(
            operationId = "registerRestaurant",
            summary = "Register a restaurant",
            description = "Registers a new restaurant with its name, phone number, and address, "
                    + "associates it with the authenticated owner, and returns its identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Restaurant registered successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegisterRestaurantResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified province or ward was not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<RegisterRestaurantResponse> registerRestaurant(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody RegisterRestaurantRequest request
    ) {
        var command = new RegisterRestaurantCommand(
                user.userId(), request.name(), request.phone(), request.provinceCode(),
                request.wardCode(), request.addressLine(), request.latitude(), request.longitude()
        );
        var restaurantId = registerRestaurantUseCase.registerRestaurant(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterRestaurantResponse(restaurantId.value()));
    }
}
