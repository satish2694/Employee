package org.myapp.employee;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

public class UploadJob {

    private final String jobId;
    private final String fileName;
    private final String contentType;
    private final byte[] fileData;
    private final Instant createdAt;
    private final AtomicReference<DownloadJobStatus> status;
    private volatile String presignedDownloadUrl;
    private volatile Instant urlExpiresAt;
    private volatile String errorMessage;

    public UploadJob(String jobId, String fileName, String contentType, byte[] fileData) {
        this.jobId = jobId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileData = fileData;
        this.createdAt = Instant.now();
        this.status = new AtomicReference<>(DownloadJobStatus.PENDING);
    }

    public String getJobId() { return jobId; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public byte[] getFileData() { return fileData; }
    public Instant getCreatedAt() { return createdAt; }
    public DownloadJobStatus getStatus() { return status.get(); }
    public String getPresignedDownloadUrl() { return presignedDownloadUrl; }
    public Instant getUrlExpiresAt() { return urlExpiresAt; }
    public String getErrorMessage() { return errorMessage; }

    public void markInProgress() {
        status.set(DownloadJobStatus.IN_PROGRESS);
    }

    public void markCompleted(String presignedUrl, Instant expiresAt) {
        this.presignedDownloadUrl = presignedUrl;
        this.urlExpiresAt = expiresAt;
        status.set(DownloadJobStatus.COMPLETED);
    }

    public void markFailed(String error) {
        this.errorMessage = error;
        status.set(DownloadJobStatus.FAILED);
    }
}
