package com.mealgo.restaurantservice.application.port.in.upload;

public interface GenerateUploadUrlUseCase {
    PresignedUploadResult generateUploadUrl(GenerateUploadUrlCommand command);
}
