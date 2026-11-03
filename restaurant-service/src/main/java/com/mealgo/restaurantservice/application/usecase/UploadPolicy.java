package com.mealgo.restaurantservice.application.usecase;

public record UploadPolicy(long maxFileSize) {
    public UploadPolicy {
        if (maxFileSize <= 0) {
            throw new IllegalArgumentException("Maximum file size must be greater than zero");
        }
    }
}
