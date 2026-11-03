package com.mealgo.restaurantservice.application.port.in.upload;

import java.util.UUID;

public record GenerateUploadUrlCommand(
        UUID restaurantId,
        UUID ownerId,
        String fileName,
        String contentType,
        Long fileSize
) {
}
