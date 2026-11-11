package com.mealgo.restaurantservice.api.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAvatarRequest(@NotBlank @Size(max = 2048) String avatarUrl) {}
