package com.mealgo.restaurantservice.api.rest.dto.request;

public record GenerateUploadUrlRequest(
        String fileName,
        String contentType,
        Long fileSize
) {
}
