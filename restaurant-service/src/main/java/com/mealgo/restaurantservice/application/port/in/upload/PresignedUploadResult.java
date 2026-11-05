package com.mealgo.restaurantservice.application.port.in.upload;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record PresignedUploadResult(
        String uploadUrl,
        String objectKey,
        String imageUrl,
        Instant expiresAt,
        String method,
        Map<String, List<String>> requiredHeaders
) {
}
