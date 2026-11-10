package com.mealgo.restaurantservice.application.port.in.restaurant;

import com.mealgo.restaurantservice.domain.enums.ApprovalStatus;
import java.util.UUID;

public record ReviewApplicationCommand(UUID restaurantId, UUID adminId, ApprovalStatus decision, String reason) {
}
