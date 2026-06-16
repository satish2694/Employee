package org.myapp.employee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.presigner.model.PresignedDeleteObjectRequest;

/**
 * Service for direct S3 file uploads
 * Uploads files directly to S3 and returns presigned delete URLs
 */
@Service
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    private final S3FileStorageGateway s3FileStorageGateway;

    @Value("${aws.s3.bucket}") private String bucket;

    public FileUploadService(S3FileStorageGateway s3FileStorageGateway) {
        this.s3FileStorageGateway = s3FileStorageGateway;
    }

    /**
     * Upload a file to S3 and get presigned delete URL
     *
     * @param fileName    Name of the file
     * @param contentType MIME type of the file
     * @param fileData    File content as bytes
     * @return PresignedDeleteResponse with delete URL
     */
    public PresignedDeleteResponse uploadFileAndGetPresignedDelete(String fileName, String contentType, byte[] fileData) {
        try {
            logger.info("Starting S3 upload for file: {}, Size: {} bytes", fileName, fileData != null ? fileData.length : 0);

            // Upload file to S3
            s3FileStorageGateway.uploadToS3(bucket, fileName, contentType, fileData);
            logger.info("✓ File successfully uploaded to S3. FileName: {}", fileName);

            // Generate presigned delete URL
            PresignedDeleteObjectRequest presignedDeleteRequest = s3FileStorageGateway
                    .generatePresignedDeleteRequest(bucket, fileName);

            logger.info("✓ Presigned delete URL generated. File: {}, URL: {}", 
                    fileName, presignedDeleteRequest.url());

            return new PresignedDeleteResponse(
                    null,
                    fileName,
                    presignedDeleteRequest.url().toString(),
                    null,
                    null
            );

        } catch (SdkException e) {
            logger.error("✗ AWS SDK Error during file upload: {}", e.getMessage(), e);
            throw new RuntimeException("AWS SDK Error: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("✗ Error during file upload: {}", e.getMessage(), e);
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }
    }
}
