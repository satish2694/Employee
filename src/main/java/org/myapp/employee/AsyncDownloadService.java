package org.myapp.employee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AsyncDownloadService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncDownloadService.class);

    private final S3FileStorageGateway s3FileStorageGateway;
    private final ConcurrentHashMap<String, DownloadJob> jobStore = new ConcurrentHashMap<>();

    @Value("${aws.s3.bucket}")
    private String bucket;

    public AsyncDownloadService(S3FileStorageGateway s3FileStorageGateway) {
        this.s3FileStorageGateway = s3FileStorageGateway;
    }

    /**
     * Creates a download job and returns its ID immediately.
     * The actual S3 work happens asynchronously via {@link #processDownload}.
     */
    public String initiateDownload(String fileName) {
        String jobId = UUID.randomUUID().toString();
        DownloadJob job = new DownloadJob(jobId, fileName);
        jobStore.put(jobId, job);
        logger.info("Download job created. jobId={}, fileName={}", jobId, fileName);
        processDownload(jobId, fileName);
        return jobId;
    }

    /**
     * Retrieves a job by ID, or null if not found.
     */
    public DownloadJob getJob(String jobId) {
        return jobStore.get(jobId);
    }

    @Async("downloadTaskExecutor")
    public void processDownload(String jobId, String fileName) {
        DownloadJob job = jobStore.get(jobId);
        if (job == null) {
            logger.warn("processDownload called for unknown jobId={}", jobId);
            return;
        }

        job.markInProgress();
        logger.info("Processing download. jobId={}, fileName={}", jobId, fileName);

        try {
            // Verify the object exists before generating a presigned URL
            s3FileStorageGateway.assertObjectExists(bucket, fileName);

            PresignedGetObjectRequest presignedRequest =
                    s3FileStorageGateway.generatePresignedGetRequest(bucket, fileName);

            Instant expiresAt = presignedRequest.expiration();
            job.markCompleted(presignedRequest.url().toString(), expiresAt);

            logger.info("Download job completed. jobId={}, expiresAt={}", jobId, expiresAt);

        } catch (NoSuchKeyException e) {
            String msg = "File not found in S3: " + fileName;
            job.markFailed(msg);
            logger.warn("Download job failed — file not found. jobId={}, fileName={}", jobId, fileName);
        } catch (Exception e) {
            String msg = "Failed to generate download URL: " + e.getMessage();
            job.markFailed(msg);
            logger.error("Download job failed. jobId={}, error={}", jobId, e.getMessage(), e);
        }
    }
}
