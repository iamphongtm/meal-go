package com.mealgo.restaurant_service.controller;

import com.mealgo.restaurant_service.dto.request.CategoryCreationRequest;
import com.mealgo.restaurant_service.dto.response.CategoryCreationResponse;
import com.mealgo.restaurant_service.service.MenuInitializationService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Menu Categories", description = "APIs for managing restaurant menu categories")
public class MenuCategoryController {
    private final MenuInitializationService menuInitializationService;

    @Operation(
            operationId = "createMenuCategory",
            summary = "Create a menu category",
            description = "Creates a category under a restaurant. Display order is assigned automatically."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Menu category created successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Restaurant not found",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Category name already exists in the restaurant",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping("/restaurants/{restaurantId}/menu-categories")
    public ResponseEntity<CategoryCreationResponse> createCategory(
            @Parameter(description = "Restaurant ID", required = true)
            @PathVariable UUID restaurantId,
            @Valid @RequestBody CategoryCreationRequest request
    ) {
        CategoryCreationResponse response = menuInitializationService.createCategory(
                restaurantId,
                request
        );
        URI location = URI.create("/menu-categories/" + response.id());
        return ResponseEntity.created(location).body(response);
    }
}
