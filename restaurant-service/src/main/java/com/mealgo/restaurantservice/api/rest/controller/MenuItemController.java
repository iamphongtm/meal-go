package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.api.rest.dto.request.GenerateUploadUrlRequest;
import com.mealgo.restaurantservice.api.rest.dto.request.MenuItemCreationRequest;
import com.mealgo.restaurantservice.api.rest.dto.response.MenuItemCreationResponse;
import com.mealgo.restaurantservice.application.port.in.menuitem.CreateMenuItemCommand;
import com.mealgo.restaurantservice.application.port.in.menuitem.CreateMenuItemUseCase;
import com.mealgo.restaurantservice.application.port.in.menuitem.UpdateMenuItemUseCase;
import com.mealgo.restaurantservice.application.port.in.menuitem.UpdateMenuItemCommand;
import com.mealgo.restaurantservice.application.port.in.menuitem.UpdateMenuItemStatusCommand;
import com.mealgo.restaurantservice.api.rest.dto.request.UpdateMenuItemRequest;
import com.mealgo.restaurantservice.api.rest.dto.request.UpdateMenuItemStatusRequest;
import com.mealgo.restaurantservice.api.rest.dto.response.PresignedUploadResponse;
import com.mealgo.restaurantservice.application.port.in.upload.GenerateUploadUrlCommand;
import com.mealgo.restaurantservice.application.port.in.upload.GenerateUploadUrlUseCase;
import com.mealgo.restaurantservice.infrastructure.config.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/restaurants/{restaurantId}/menu-items")
@Tag(name = "Menu Items", description = "Restaurant menu items")
public class MenuItemController {
    private final GenerateUploadUrlUseCase generateUploadUrlUseCase;
    private final CreateMenuItemUseCase createMenuItemUseCase;
    private final UpdateMenuItemUseCase updateMenuItemUseCase;

    @PutMapping("/{menuItemId}")
    @Operation(summary = "Update menu item details without changing linked option groups")
    public ResponseEntity<Void> updateItem(@PathVariable UUID restaurantId, @PathVariable UUID menuItemId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody UpdateMenuItemRequest request) {
        updateMenuItemUseCase.update(new UpdateMenuItemCommand(restaurantId, user.userId(), menuItemId,
                request.categoryId(), request.name(), request.description(), request.basePrice(), request.imageUrl()));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{menuItemId}/status")
    @Operation(summary = "Set menu item status to ACTIVE, SOLD_OUT or INACTIVE")
    public ResponseEntity<Void> updateItemStatus(@PathVariable UUID restaurantId, @PathVariable UUID menuItemId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody UpdateMenuItemStatusRequest request) {
        updateMenuItemUseCase.updateStatus(new UpdateMenuItemStatusCommand(
                restaurantId, user.userId(), menuItemId, request.status()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @Operation(
            summary = "Create a menu item",
            description = """
                    Creates a menu item in the specified restaurant and category.
                    Option groups must already exist and belong to the restaurant. Omit optionGroups or
                    provide an empty list to create an item without options. Additional prices are
                    specific to this menu item; options without an explicit price default to zero.
                    """)
    public ResponseEntity<MenuItemCreationResponse> createMenuItem(
            @PathVariable UUID restaurantId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody MenuItemCreationRequest request) {
        var groups = request.optionGroups() == null ? List.<CreateMenuItemCommand.OptionGroupCommand>of()
                : request.optionGroups().stream().map(group -> new CreateMenuItemCommand.OptionGroupCommand(
                        group.optionGroupId(), group.minSelect(), group.maxSelect(),
                        group.optionPrices() == null ? List.of() : group.optionPrices().stream()
                                .map(price -> new CreateMenuItemCommand.OptionPriceCommand(
                                        price.optionId(), price.additionalPrice()))
                                .toList()))
                .toList();

        var command = new CreateMenuItemCommand(
                restaurantId, user.userId(), request.categoryId(), request.name(), request.description(),
                request.basePrice(), request.imageUrl(), groups);

        var menuItemId = createMenuItemUseCase.createMenuItem(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MenuItemCreationResponse(menuItemId.value()));
    }

    @PostMapping("/uploads")
    @Operation(summary = "Generate a presigned URL to upload a menu item image")
    public ResponseEntity<PresignedUploadResponse> generateUploadUrl(
            @PathVariable UUID restaurantId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody GenerateUploadUrlRequest request) {
        var command = new GenerateUploadUrlCommand(
                restaurantId, user.userId(), request.fileName(), request.contentType(), request.fileSize());

        var result = generateUploadUrlUseCase.generateUploadUrl(command);

        return ResponseEntity.ok(new PresignedUploadResponse(
                result.uploadUrl(), result.objectKey(), result.imageUrl(), result.expiresAt(), result.method(), result.requiredHeaders()));
    }
}
