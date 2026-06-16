package org.myapp.employee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AsyncUploadService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncUploadService.class);

    private final S3FileStorageGateway s3FileStorageGateway;
    private final ConcurrentHashMap<String, UploadJob> jobStore = new ConcurrentHashMap<>();

    @Value("${aws.s3.bucket}")
    private String bucket;

    public AsyncUploadService(S3FileStorageGateway s3FileStorageGateway) {
        this.s3FileStorageGateway = s3FileStorageGateway;
    }

    /**
     * Stores the file bytes in memory, creates an upload job, and returns the jobId immediately.
     * The actual S3 upload happens asynchronously on a background thread via {@link #processUpload}.
     */
    public String initiateUpload(String fileName, String contentType, byte[] fileData) {
        String jobId = UUID.randomUUID().toString();
        UploadJob job = new UploadJob(jobId, fileName, contentType, fileData);
        jobStore.put(jobId, job);
        logger.info("Upload job created. jobId={}, fileName={}, size={} bytes", jobId, fileName, fileData.length);
        processUpload(jobId);
        return jobId;
    }

    public UploadJob getJob(String jobId) {
        return jobStore.get(jobId);
    }

    @Async("downloadTaskExecutor")
    public void processUpload(String jobId) {
        UploadJob job = jobStore.get(jobId);
        if (job == null) {
            logger.warn("processUpload called for unknown jobId={}", jobId);
            return;
        }

        job.markInProgress();
        logger.info("Uploading to S3. jobId={}, fileName={}", jobId, job.getFileName());

        try {
            s3FileStorageGateway.uploadToS3(bucket, job.getFileName(), job.getContentType(), job.getFileData());
            logger.info("S3 upload complete. jobId={}, fileName={}", jobId, job.getFileName());

            PresignedGetObjectRequest presigned =
                    s3FileStorageGateway.generatePresignedGetRequest(bucket, job.getFileName());

            Instant expiresAt = presigned.expiration();
            job.markCompleted(presigned.url().toString(), expiresAt);
            logger.info("Upload job completed. jobId={}, expiresAt={}", jobId, expiresAt);

        } catch (Exception e) {
            job.markFailed("Upload failed: " + e.getMessage());
            logger.error("Upload job failed. jobId={}, error={}", jobId, e.getMessage(), e);
        }
    }
}
