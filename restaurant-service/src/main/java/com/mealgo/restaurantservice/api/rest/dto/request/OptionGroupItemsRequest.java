package com.mealgo.restaurantservice.api.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OptionGroupItemsRequest(@NotNull List<@NotNull @Valid Item> items) {
    public record Item(@NotNull UUID menuItemId, @NotNull @Min(0) Integer minSelect,
                       @NotNull @Min(1) Integer maxSelect,
                       @NotNull List<@NotNull @Valid Price> optionPrices) {}
    public record Price(@NotNull UUID optionId,
                        @NotNull @DecimalMin("0") @Digits(integer = 17, fraction = 2) BigDecimal additionalPrice) {}
}
