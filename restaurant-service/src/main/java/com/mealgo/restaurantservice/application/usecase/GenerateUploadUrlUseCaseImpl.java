package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.port.in.upload.GenerateUploadUrlCommand;
import com.mealgo.restaurantservice.application.port.in.upload.GenerateUploadUrlUseCase;
import com.mealgo.restaurantservice.application.port.in.upload.PresignedUploadResult;
import com.mealgo.restaurantservice.application.port.out.ImageStorage;
import com.mealgo.restaurantservice.application.port.out.RestaurantRepository;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

import static com.mealgo.restaurantservice.domain.shared.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class GenerateUploadUrlUseCaseImpl implements GenerateUploadUrlUseCase {
    private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final RestaurantRepository restaurantRepository;
    private final ImageStorage imageStorage;
    private final UploadPolicy uploadPolicy;

    @Override
    public PresignedUploadResult generateUploadUrl(GenerateUploadUrlCommand command) {
        validateCommand(command);

        if (!restaurantRepository.existsByIdAndOwnerId(command.restaurantId(), command.ownerId())) {
            throw new BusinessException(RESTAURANT_NOT_FOUND, command.restaurantId());
        }

        String objectKey = "restaurants/%s/images/%s%s".formatted(
                command.restaurantId(), UUID.randomUUID(), IMAGE_EXTENSIONS.get(command.contentType()));

        return imageStorage.generateUploadUrl(objectKey, command.contentType());
    }

    private void validateCommand(GenerateUploadUrlCommand command) {
        if (command == null) {
            throw new BusinessException(UPLOAD_REQUEST_REQUIRED);
        }
        if (command.fileName() == null || command.fileName().isBlank() || command.fileName().length() > 255) {
            throw new BusinessException(UPLOAD_FILE_NAME_INVALID);
        }
        if (command.contentType() == null || !IMAGE_EXTENSIONS.containsKey(command.contentType())) {
            throw new BusinessException(UPLOAD_CONTENT_TYPE_INVALID);
        }
        if (command.fileSize() == null || command.fileSize() <= 0 || command.fileSize() > uploadPolicy.maxFileSize()) {
            throw new BusinessException(UPLOAD_FILE_SIZE_INVALID, uploadPolicy.maxFileSize());
        }
    }
}
