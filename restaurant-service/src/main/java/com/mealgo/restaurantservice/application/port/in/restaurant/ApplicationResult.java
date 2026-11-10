package com.mealgo.restaurantservice.application.port.in.restaurant;

import com.mealgo.restaurantservice.domain.enums.ApprovalStatus;
import java.time.Instant;
import java.util.UUID;

public record ApplicationResult(UUID restaurantId, UUID ownerId, String name, String phone,
        String addressLine, Integer wardCode, Double latitude, Double longitude,
        ApprovalStatus approvalStatus, Instant submittedAt, UUID reviewedBy,
        Instant reviewedAt, String rejectionReason) {
}
