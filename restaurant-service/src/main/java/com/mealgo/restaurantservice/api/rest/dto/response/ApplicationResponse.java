package com.mealgo.restaurantservice.api.rest.dto.response;

import com.mealgo.restaurantservice.domain.enums.ApprovalStatus;
import java.time.Instant;
import java.util.UUID;

public record ApplicationResponse(UUID restaurantId, UUID ownerId, String name, String phone,
        String addressLine, Integer wardCode, Double latitude, Double longitude,
        ApprovalStatus approvalStatus, Instant submittedAt, UUID reviewedBy,
        Instant reviewedAt, String rejectionReason) {
}
