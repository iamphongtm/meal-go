package com.mealgo.restaurantservice.infrastructure.config;

import com.mealgo.restaurantservice.application.usecase.UploadPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(AWSProperties.class)
public class S3Config {
    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner(AWSProperties properties) {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(properties.getS3().getRegion()));

        if (StringUtils.hasText(properties.getAccessKey()) && StringUtils.hasText(properties.getSecretKey())) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())));
        }

        return builder.build();
    }

    @Bean
    public UploadPolicy uploadPolicy(AWSProperties properties) {
        return new UploadPolicy(properties.getS3().getMaxFileSize().toBytes());
    }
}
