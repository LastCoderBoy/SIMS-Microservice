package com.sims.simscoreservice.shared.s3.service;

import com.sims.common.exceptions.ResourceNotFoundException;
import com.sims.simscoreservice.exceptions.CustomS3Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

/**
 * Service class for managing AWS S3 object storage operations.
 * <p>
 * This service provides a high-level abstraction for interacting with S3, offering methods
 * to upload, download, delete, and generate secure presigned URLs for objects.
 * It handles resource cleanup, error logging, and wraps AWS SDK exceptions into
 * application-specific custom exceptions.
 * </p>
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private static final Duration MAX_PRESIGNED_URL_DURATION = Duration.ofDays(7);

    public String uploadFile(String objectKey, byte[] fileBytes, String contentType) {
        try {
            log.info("[S3-SERVICE] Uploading file to S3: {} ({} bytes)", objectKey, fileBytes.length);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    .contentLength((long) fileBytes.length)
                    .metadata(Map.of(
                            "upload-timestamp", String.valueOf(System.currentTimeMillis()),
                            "content-length", String.valueOf(fileBytes.length),
                            "uploaded-by", "sims-core-service"
                    ))
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));

            log.info("[S3-SERVICE] Successfully uploaded {} to S3 bucket {}", objectKey, bucketName);
            return objectKey;

        } catch (S3Exception e) {
            log.error("[S3-SERVICE] S3 error uploading file {}: {} - {}",
                    objectKey, e.awsErrorDetails().errorCode(), e.awsErrorDetails().errorMessage());
            throw new CustomS3Exception("Failed to upload file to S3: " + e.awsErrorDetails().errorMessage(), e);
        } catch (AwsServiceException e) {
            log.error("[S3-SERVICE] AWS service error uploading file {}: {}", objectKey, e.getMessage());
            throw new CustomS3Exception("AWS service error during upload", e);
        } catch (SdkClientException e) {
            log.error("[S3-SERVICE] SDK client error uploading file {}: {}", objectKey, e.getMessage());
            throw new CustomS3Exception("Failed to communicate with S3", e);
        } catch (Exception e) {
            log.error("[S3-SERVICE] Unexpected error uploading file {}: {}", objectKey, e.getMessage(), e);
            throw new CustomS3Exception("Unexpected error uploading file to S3", e);
        }
    }

    // DOWNLOAD OPERATIONS
    public byte[] readFile(String objectKey) throws IOException {
        try {
            log.info("[S3-SERVICE] Reading file from S3: {}", objectKey);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            try (ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getObjectRequest)) {
                byte[] fileContent = s3ObjectStream.readAllBytes();
                log.info("[S3-SERVICE] Successfully read {} from S3 ({} bytes)", objectKey, fileContent.length);
                return fileContent;
            }

        } catch (NoSuchKeyException e) {
            log.error("[S3-SERVICE] File not found in S3: {}", objectKey);
            throw new ResourceNotFoundException("File not found in S3:  " + objectKey);
        } catch (S3Exception e) {
            log.error("[S3-SERVICE] S3 error reading file {}: {}", objectKey, e.awsErrorDetails().errorMessage());
            throw new CustomS3Exception("Failed to read file from S3: " + e.awsErrorDetails().errorMessage(), e);
        } catch (IOException e) {
            log.error("[S3-SERVICE] IO error reading file {}: {}", objectKey, e.getMessage());
            throw new CustomS3Exception("Failed to read file content from S3", e);
        } catch (Exception e) {
            log.error("[S3-SERVICE] Unexpected error reading file {}: {}", objectKey, e.getMessage(), e);
            throw new CustomS3Exception("Unexpected error reading file from S3", e);
        }
    }

    public void deleteFile(String objectKey) {
        try {
            log.info("[S3-SERVICE] Deleting file from S3: {}", objectKey);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteRequest);

            log.info("[S3-SERVICE] Successfully deleted {} from S3 bucket {}", objectKey, bucketName);

        } catch (S3Exception e) {
            log.error("[S3-SERVICE] S3 error deleting file {}: {}", objectKey, e.awsErrorDetails().errorMessage());
            throw new CustomS3Exception("Failed to delete file from S3: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            log.error("[S3-SERVICE] Unexpected error deleting file {}: {}", objectKey, e.getMessage(), e);
            throw new CustomS3Exception("Unexpected error deleting file from S3", e);
        }
    }

    public String generatePresignedUrl(String objectKey, Duration duration) {
        // Validate duration
        if (duration.compareTo(MAX_PRESIGNED_URL_DURATION) > 0) {
            throw new IllegalArgumentException(
                    "Presigned URL duration cannot exceed 7 days. Requested:  " + duration.toDays() + " days");
        }

        try {
            log.info("[S3-SERVICE] Generating presigned URL for {} (valid for {} minutes)",
                    objectKey, duration.toMinutes());

            // Verify object exists first
            if (!objectExists(objectKey)) {
                throw new ResourceNotFoundException("Object not found in S3: " + objectKey);
            }

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String url = presignedRequest.url().toString();

            log.info("[S3-SERVICE] Generated presigned URL for {} (expires in {} minutes)",
                    objectKey, duration.toMinutes());

            return url;

        } catch (NoSuchKeyException | ResourceNotFoundException e) {
            log.error("[S3-SERVICE] Object not found in S3: {}", objectKey);
            throw new ResourceNotFoundException("QR code image not found:  " + objectKey);
        } catch (S3Exception e) {
            log.error("[S3-SERVICE] S3 error generating presigned URL for {}: {}",
                    objectKey, e.awsErrorDetails().errorMessage());
            throw new CustomS3Exception("Failed to generate presigned URL:  " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            log.error("[S3-SERVICE] Unexpected error generating presigned URL for {}:  {}",
                    objectKey, e.getMessage(), e);
            throw new CustomS3Exception("Unexpected error generating presigned URL", e);
        }
    }

    public boolean objectExists(String objectKey) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.headObject(headRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("[S3-SERVICE] Error checking if object exists {}: {}", objectKey, e.getMessage());
            return false;
        }
    }
}
