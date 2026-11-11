package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.api.rest.dto.request.CategoryCreationRequest;
import com.mealgo.restaurantservice.api.rest.dto.response.CategoryCreationResponse;
import com.mealgo.restaurantservice.application.port.in.menucategory.ManageMenuCategoryUseCase;
import com.mealgo.restaurantservice.application.port.in.menucategory.CreateMenuCategoryCommand;
import com.mealgo.restaurantservice.domain.model.category.MenuCategoryId;
import com.mealgo.restaurantservice.infrastructure.config.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@org.springframework.security.access.prepost.PreAuthorize("hasRole('RESTAURANT')")
@Tag(name = "Menu Categories", description = "APIs for managing restaurant menu categories")
@RequiredArgsConstructor
public class MenuCategoryController {
    private final ManageMenuCategoryUseCase menuCategoryUseCase;

    @Operation(
            operationId = "createMenuCategory",
            summary = "Create a menu category",
            description = "Creates a category under a restaurant. Display order is assigned automatically."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Menu category created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CategoryCreationResponse.class))),
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
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CategoryCreationRequest request) {

        CreateMenuCategoryCommand command = CreateMenuCategoryCommand.builder().name(request.name()).restaurantId(restaurantId).ownerId(user.userId()).build();
        MenuCategoryId result = menuCategoryUseCase.create(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CategoryCreationResponse(result.value()));
    }
}
