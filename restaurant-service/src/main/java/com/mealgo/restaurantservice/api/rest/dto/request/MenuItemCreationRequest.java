package com.mealgo.restaurantservice.api.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MenuItemCreationRequest(
        @NotNull(message = "{menu-item.category.required}") UUID categoryId,
        @NotBlank(message = "{menu-item.name.required}")
        @Size(max = 255, message = "{menu-item.name.max-length}") String name,
        @Size(max = 255, message = "{menu-item.description.max-length}") String description,
        @NotNull(message = "{menu-item.base-price.required}")
        @DecimalMin(value = "0", message = "{menu-item.price.non-negative}")
        @Digits(integer = 17, fraction = 2, message = "{menu-item.price.digits}") BigDecimal basePrice,
        @Size(max = 255, message = "{menu-item.image-url.max-length}") String imageUrl,
        List<@NotNull(message = "{menu-item.option-group.required}") @Valid OptionGroupRequest> optionGroups
) {
    public record OptionGroupRequest(
            @NotNull(message = "{menu-item.option-group-id.required}") UUID optionGroupId,
            @NotNull(message = "{menu-item.min-select.required}")
            @Min(value = 0, message = "{menu-item.selection.non-negative}") Integer minSelect,
            @NotNull(message = "{menu-item.max-select.required}")
            @Min(value = 0, message = "{menu-item.selection.non-negative}") Integer maxSelect,
            List<@NotNull(message = "{menu-item.option-price.required}") @Valid OptionPriceRequest> optionPrices
    ) {
    }

    public record OptionPriceRequest(
            @NotNull(message = "{menu-item.option-id.required}") UUID optionId,
            @NotNull(message = "{menu-item.additional-price.required}")
            @DecimalMin(value = "0", message = "{menu-item.price.non-negative}")
            @Digits(integer = 17, fraction = 2, message = "{menu-item.price.digits}") BigDecimal additionalPrice
    ) {
    }
}
