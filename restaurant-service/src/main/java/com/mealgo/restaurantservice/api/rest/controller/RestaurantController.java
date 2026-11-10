package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.api.rest.dto.request.UpdateRestaurantAddressRequest;
import com.mealgo.restaurantservice.api.rest.dto.response.RestaurantAddressResponse;
import com.mealgo.restaurantservice.api.rest.mapper.RestaurantAddressApiMapper;
import com.mealgo.restaurantservice.application.port.in.restaurant.UpdateRestaurantAddressCommand;
import com.mealgo.restaurantservice.application.port.in.restaurant.UpdateRestaurantAddressUseCase;
import com.mealgo.restaurantservice.application.port.in.restaurant.RestaurantAddressResult;

import com.mealgo.restaurantservice.application.port.in.restaurant.RegisterRestaurantUseCase;
import com.mealgo.restaurantservice.application.port.in.restaurant.GetMyRestaurantUseCase;
import com.mealgo.restaurantservice.application.port.in.restaurant.MyRestaurantResult;
import com.mealgo.restaurantservice.application.port.in.restaurant.UpdateRestaurantStatusUseCase;
import com.mealgo.restaurantservice.application.port.in.restaurant.UpdateRestaurantStatusCommand;
import com.mealgo.restaurantservice.application.port.in.restaurant.RestaurantStatusResult;
import com.mealgo.restaurantservice.api.rest.dto.request.UpdateRestaurantStatusRequest;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;
import com.mealgo.restaurantservice.application.port.in.restaurant.RegisterRestaurantCommand;
import com.mealgo.restaurantservice.api.rest.dto.request.RegisterRestaurantRequest;
import com.mealgo.restaurantservice.api.rest.dto.response.RegisterRestaurantResponse;
import com.mealgo.restaurantservice.infrastructure.config.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.springframework.web.bind.annotation.GetMapping;
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
    GetMyRestaurantUseCase getMyRestaurantUseCase;
    UpdateRestaurantStatusUseCase updateRestaurantStatusUseCase;
    UpdateRestaurantAddressUseCase updateRestaurantAddressUseCase;
    RestaurantAddressApiMapper restaurantAddressApiMapper;

    @PatchMapping("/{restaurantId}/address")
    @PreAuthorize("hasRole('RESTAURANT')")
    @Operation(summary = "Update the address of an approved, closed restaurant")
    public ResponseEntity<RestaurantAddressResponse> updateAddress(
            @PathVariable UUID restaurantId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody UpdateRestaurantAddressRequest request
    ) {
        UpdateRestaurantAddressCommand command = restaurantAddressApiMapper.toCommand(
                restaurantId,
                user.userId(),
                request
        );

        RestaurantAddressResult result = updateRestaurantAddressUseCase.updateAddress(command);
        RestaurantAddressResponse response = restaurantAddressApiMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{restaurantId}/status")
    @PreAuthorize("hasRole('RESTAURANT')")
    @Operation(summary = "Update restaurant order acceptance status")
    public RestaurantStatusResult updateStatus(
            @PathVariable UUID restaurantId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody UpdateRestaurantStatusRequest request) {
        return updateRestaurantStatusUseCase.updateStatus(
                new UpdateRestaurantStatusCommand(
                        restaurantId, user.userId(), request.status(), request.pauseReason()));
    }

    @Operation(
            operationId = "getMyRestaurant",
            summary = "Get the authenticated owner's restaurant",
            description = "Returns the restaurant identifier, name, and status for the authenticated owner."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restaurant found"),
            @ApiResponse(responseCode = "404", description = "The authenticated owner has no restaurant",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RESTAURANT')")
    public MyRestaurantResult getMyRestaurant(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return getMyRestaurantUseCase.getMyRestaurant(user.userId());
    }

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
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RESTAURANT')")
    public ResponseEntity<RegisterRestaurantResponse> registerRestaurant(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody RegisterRestaurantRequest request
    ) {
        RegisterRestaurantCommand command = new RegisterRestaurantCommand(
                user.userId(), request.name(), request.phone(), request.provinceCode(),
                request.wardCode(), request.addressLine(), request.latitude(), request.longitude()
        );
        com.mealgo.restaurantservice.domain.model.restaurant.RestaurantId restaurantId = registerRestaurantUseCase.registerRestaurant(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterRestaurantResponse(restaurantId.value()));
    }
}
