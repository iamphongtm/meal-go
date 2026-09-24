package com.mealgo.restaurant_service.controller;

import com.mealgo.restaurant_service.dto.request.RestaurantCreationRequest;
import com.mealgo.restaurant_service.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurants", description = "APIs for initializing and managing restaurants")
public class RestaurantController {
    private final RestaurantService restaurantService;

    @Operation(
            operationId = "createRestaurant",
            summary = "Create a restaurant",
            description = "Creates a restaurant for the owner supplied in the temporary X-Owner-Id header."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Restaurant created successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Province or ward not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<Void> createRestaurant(
            @Parameter(
                    description = "Temporary owner ID until authentication is implemented",
                    required = true,
                    example = "7f901364-615c-4176-a587-1045ec26981f"
            )
            @RequestHeader("X-Owner-Id") UUID ownerId,
            @Valid @RequestBody RestaurantCreationRequest request
    ) {
        restaurantService.init(request, ownerId);
        return ResponseEntity.noContent().build();
    }
}
