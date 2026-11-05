package com.mealgo.restaurantservice.infrastructure.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "aws")
@Validated
@Getter
@Setter
public class AWSProperties {

    private String secretKey;
    private String accessKey;

    @Valid
    @NotNull
    private S3 s3;

    @Getter
    @Setter
    public static class S3 {
        @NotBlank
        private String bucket;

        @NotBlank
        private String region;

        @NotNull
        private Duration uploadUrlExpiration;

        @NotNull
        @DataSizeUnit(DataUnit.BYTES)
        private DataSize maxFileSize;

        @NotBlank
        private String cdnBaseUrl;

        @AssertTrue(message = "uploadUrlExpiration must be > 0 and <= 15 minutes")
        public boolean isUploadUrlExpirationValid() {
            return uploadUrlExpiration != null
                    && uploadUrlExpiration.compareTo(Duration.ZERO) > 0
                    && uploadUrlExpiration.compareTo(Duration.ofMinutes(15)) <= 0;
        }

        @AssertTrue(message = "maxFileSize must be greater than 0")
        public boolean isMaxFileSizeValid() {
            return maxFileSize != null && maxFileSize.toBytes() > 0;
        }
    }

}
