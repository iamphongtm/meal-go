package com.mealgo.restaurant_service.controller;

import com.mealgo.restaurant_service.dto.request.OptionGroupCreationRequest;
import com.mealgo.restaurant_service.dto.response.OptionGroupCreationResponse;
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
@Tag(name = "Option Groups", description = "APIs for managing menu item option groups")
public class OptionGroupController {
    private final MenuInitializationService menuInitializationService;

    @Operation(
            operationId = "createOptionGroup",
            summary = "Create an option group",
            description = "Creates an option group under a menu item. Display order is assigned automatically."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Option group created successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or selection range",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Menu item not found",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Option group name already exists for the menu item",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping("/menu-items/{menuItemId}/option-groups")
    public ResponseEntity<OptionGroupCreationResponse> createOptionGroup(
            @Parameter(description = "Menu item ID", required = true)
            @PathVariable UUID menuItemId,
            @Valid @RequestBody OptionGroupCreationRequest request
    ) {
        OptionGroupCreationResponse response = menuInitializationService.createOptionGroup(
                menuItemId,
                request
        );
        URI location = URI.create("/option-groups/" + response.id());
        return ResponseEntity.created(location).body(response);
    }
}
