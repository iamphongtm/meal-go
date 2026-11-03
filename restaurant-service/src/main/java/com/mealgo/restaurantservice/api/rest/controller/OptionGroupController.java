package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.api.rest.dto.request.OptionGroupCreationRequest;
import com.mealgo.restaurantservice.application.port.in.optiongroup.ManageOptionGroupUseCase;
import com.mealgo.restaurantservice.application.port.in.optiongroup.OptionGroupCreationCommand;
import com.mealgo.restaurantservice.infrastructure.config.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/restaurants/{restaurantId}/option-groups")
@Tag(name = "Option Groups", description = "Shared option groups owned by a restaurant")
public class OptionGroupController {

    private final ManageOptionGroupUseCase optionGroupUseCase;

    @PostMapping
    @Operation(summary = "Create a shared option group with its options")
    public ResponseEntity<?> createOptionGroup(
            @PathVariable UUID restaurantId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody OptionGroupCreationRequest request) {

        OptionGroupCreationCommand command = OptionGroupCreationCommand.builder()
                .restaurantId(restaurantId)
                .ownerId(user.userId())
                .name(request.name())
                .options(request.options()
                        .stream()
                        .map(optionCreationRequest ->
                                OptionGroupCreationCommand.OptionCreationCommand
                                .builder()
                                        .name(optionCreationRequest.name())
                                        .build())
                        .toList())
                .build();
        var result = optionGroupUseCase.createOptionGroup(command);

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}

