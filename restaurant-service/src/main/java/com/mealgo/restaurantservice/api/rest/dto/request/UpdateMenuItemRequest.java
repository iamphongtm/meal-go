package com.mealgo.restaurantservice.api.rest.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record UpdateMenuItemRequest(@NotNull UUID categoryId,
                                    @NotBlank @Size(max = 255) String name,
                                    @Size(max = 255) String description,
                                    @NotNull @DecimalMin("0") @Digits(integer = 17, fraction = 2) BigDecimal basePrice,
                                    @Size(max = 2048) String imageUrl) {}
