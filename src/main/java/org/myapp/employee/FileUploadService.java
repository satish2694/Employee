package org.myapp.employee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    private final FileRepository fileRepository;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}") private String bucket;

    public FileUploadService(FileRepository fileRepository, S3Client s3Client) {
        this.fileRepository = fileRepository;
        this.s3Client = s3Client;
    }

    @Async
    public void uploadToS3(Long fileId) {
        try {
            logger.info("Starting async S3 upload for fileId: {}", fileId);

            FileEntity file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("File not found: " + fileId));

            logger.info("Uploading file: {} to S3 bucket: {}", file.getFileName(), bucket);

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(file.getFileName())
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getData())
            );

            logger.info("✓ File successfully uploaded to S3. Thread: {}, File: {}",
                    Thread.currentThread().getName(), file.getFileName());
            System.out.println("✓ Upload successful - Thread [" + Thread.currentThread().getName() + "] uploaded: " + file.getFileName());

        } catch (SdkException e) {
            logger.error("✗ AWS SDK Error during S3 upload: {}", e.getMessage(), e);
            System.out.println("✗ AWS Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("✗ Error during S3 upload for fileId: {}", fileId, e);
            System.out.println("✗ Upload failed: " + e.getMessage());
        }
    }
}
