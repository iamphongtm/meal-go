package com.mealgo.restaurant_service.controller;

import com.mealgo.restaurant_service.dto.request.OptionCreationRequest;
import com.mealgo.restaurant_service.dto.response.OptionCreationResponse;
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
@Tag(name = "Options", description = "APIs for managing option group choices")
public class OptionController {
    private final MenuInitializationService menuInitializationService;

    @Operation(
            operationId = "createOption",
            summary = "Create an option",
            description = "Creates an option under an option group. Display order is assigned automatically."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Option created successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Option group not found",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Option name already exists in the option group",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping("/option-groups/{optionGroupId}/options")
    public ResponseEntity<OptionCreationResponse> createOption(
            @Parameter(description = "Option group ID", required = true)
            @PathVariable UUID optionGroupId,
            @Valid @RequestBody OptionCreationRequest request
    ) {
        OptionCreationResponse response = menuInitializationService.createOption(
                optionGroupId,
                request
        );
        URI location = URI.create("/options/" + response.id());
        return ResponseEntity.created(location).body(response);
    }
}
