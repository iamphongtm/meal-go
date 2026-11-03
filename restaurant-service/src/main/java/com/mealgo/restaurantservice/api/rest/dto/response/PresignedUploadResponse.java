package com.mealgo.restaurantservice.api.rest.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record PresignedUploadResponse(
        String uploadUrl,
        String objectKey,
        Instant expiresAt,
        String method,
        Map<String, List<String>> requiredHeaders
) {
}
