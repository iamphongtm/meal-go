package com.mealgo.restaurantservice.infrastructure.storage;

import com.mealgo.restaurantservice.infrastructure.config.AWSProperties;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class S3ImageStorageAdapterTest {
    @ParameterizedTest
    @ValueSource(strings = {"https://images.example.com", "https://images.example.com/", "https://images.example.com///"})
    void returnsConfiguredCdnUrlAndPresignedUploadDetails(String cdnBaseUrl) {
        var properties = new AWSProperties();
        var s3 = new AWSProperties.S3();
        s3.setBucket("menu-images-test");
        s3.setCdnBaseUrl(cdnBaseUrl);
        s3.setUploadUrlExpiration(Duration.ofMinutes(5));
        properties.setS3(s3);

        try (var presigner = S3Presigner.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test-access-key", "test-secret-key")))
                .build()) {
            var adapter = new S3ImageStorageAdapter(presigner, properties);
            String key = "restaurants/test/menu-items/image.jpg";

            var result = adapter.generateUploadUrl(key, "image/jpeg");

            assertEquals("https://images.example.com/" + key, result.imageUrl());
            assertEquals(key, result.objectKey());
            assertEquals("PUT", result.method());
            assertTrue(result.uploadUrl().contains("X-Amz-Signature="));
            assertTrue(result.expiresAt().isAfter(Instant.now()));
            assertTrue(result.requiredHeaders().entrySet().stream().anyMatch(entry ->
                    entry.getKey().equalsIgnoreCase("content-type")
                            && entry.getValue().equals(List.of("image/jpeg"))));
            assertFalse(result.requiredHeaders().keySet().stream().anyMatch("host"::equalsIgnoreCase));
        }
    }
}
