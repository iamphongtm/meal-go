package com.mealgo.restaurantservice.api.rest.dto.request;

import com.mealgo.restaurantservice.domain.enums.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewApplicationRequest(@NotNull ApprovalStatus decision, @Size(max = 1000) String reason) {
}
