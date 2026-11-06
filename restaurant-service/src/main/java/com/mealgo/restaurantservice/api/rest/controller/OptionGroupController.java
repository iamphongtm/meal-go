package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.api.rest.dto.request.OptionGroupCreationRequest;
import com.mealgo.restaurantservice.api.rest.dto.request.OptionGroupItemsRequest;
import com.mealgo.restaurantservice.application.port.in.optiongroup.OptionGroupItems;
import com.mealgo.restaurantservice.application.usecase.ManageOptionGroupItemsUseCase;
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
    private final ManageOptionGroupItemsUseCase groupItemsUseCase;

    @GetMapping
    @Operation(summary = "List shared option groups, active options and linked menu items")
    public java.util.List<OptionGroupItems> list(
            @PathVariable UUID restaurantId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
        return groupItemsUseCase.list(restaurantId, user.userId());
    }

    @PutMapping("/{optionGroupId}/menu-items")
    @Operation(summary = "Replace the menu items linked to this group only")
    public ResponseEntity<Void> replaceItems(
            @PathVariable UUID restaurantId, @PathVariable UUID optionGroupId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody OptionGroupItemsRequest request) {
        var items = request.items().stream().map(item -> new OptionGroupItems.ItemConfiguration(
                item.menuItemId(), item.minSelect(), item.maxSelect(), item.optionPrices().stream()
                .map(price -> new OptionGroupItems.OptionPrice(price.optionId(), price.additionalPrice()))
                .toList())).toList();
        groupItemsUseCase.replace(restaurantId, user.userId(), optionGroupId, items);
        return ResponseEntity.noContent().build();
    }

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

