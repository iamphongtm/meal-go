package com.mealgo.restaurantservice.infrastructure.storage;

import com.mealgo.restaurantservice.application.port.in.upload.PresignedUploadResult;
import com.mealgo.restaurantservice.application.port.out.ImageStorage;
import com.mealgo.restaurantservice.infrastructure.config.AWSProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class S3ImageStorageAdapter implements ImageStorage {
    private final S3Presigner s3Presigner;
    private final AWSProperties awsProperties;

    @Override
    public PresignedUploadResult generateUploadUrl(String objectKey, String contentType) {
        var putRequest = PutObjectRequest.builder()
                .bucket(awsProperties.getS3().getBucket())
                .key(objectKey)
                .contentType(contentType)
                .build();
        var presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(awsProperties.getS3().getUploadUrlExpiration())
                .putObjectRequest(putRequest)
                .build();
        var signedRequest = s3Presigner.presignPutObject(presignRequest);

        Map<String, List<String>> requiredHeaders = new LinkedHashMap<>();
        signedRequest.signedHeaders().forEach((name, values) -> {
            if (!name.equalsIgnoreCase("host")) {
                requiredHeaders.put(name, List.copyOf(values));
            }
        });

        String imageUrl = awsProperties.getS3().getCdnBaseUrl().strip().replaceAll("/+$", "") + "/" + objectKey;

        return new PresignedUploadResult(
                signedRequest.url().toExternalForm(), objectKey, imageUrl, signedRequest.expiration(),
                "PUT", Map.copyOf(requiredHeaders));
    }
}
