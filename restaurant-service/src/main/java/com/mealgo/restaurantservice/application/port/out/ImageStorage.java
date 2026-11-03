package com.mealgo.restaurantservice.application.port.out;

import com.mealgo.restaurantservice.application.port.in.upload.PresignedUploadResult;

public interface ImageStorage {
    PresignedUploadResult generateUploadUrl(String objectKey, String contentType);
}
