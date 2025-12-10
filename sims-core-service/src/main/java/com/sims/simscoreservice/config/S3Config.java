package com.sims.simscoreservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Configuration class for AWS S3 integration.
 * <p>
 * This class initializes the S3Client and S3Presigner beans using credentials
 * and region settings defined in the application properties.
 * It serves as the central configuration point for all S3-related connectivity.
 * </p>
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Configuration
@Slf4j
public class S3Config {

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /**
     * S3 Client Bean
     * Used for all S3 operations (upload, download, delete, list)
     */
    @Bean
    public S3Client s3Client() {
        try {
            log.info("[S3-CONFIG] Initializing S3 Client for region: {}, bucket: {}", region, bucketName);

            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

            S3Client client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            log.info("[S3-CONFIG] S3 Client initialized successfully");
            return client;

        } catch (Exception e) {
            log.error("[S3-CONFIG] Failed to initialize S3 Client: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize S3 Client", e);
        }
    }

    /**
     * S3 Presigner Bean
     * Used for generating temporary presigned URLs
     * Allows secure file sharing without exposing credentials
     */
    @Bean
    public S3Presigner s3Presigner() {
        try {
            log.info("[S3-CONFIG] Initializing S3 Presigner for region: {}", region);

            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

            S3Presigner presigner = S3Presigner.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            log.info("[S3-CONFIG] S3 Presigner initialized successfully");
            return presigner;

        } catch (Exception e) {
            log.error("[S3-CONFIG] Failed to initialize S3 Presigner: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize S3 Presigner", e);
        }
    }
}
