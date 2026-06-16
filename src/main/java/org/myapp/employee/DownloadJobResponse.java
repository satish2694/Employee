package org.myapp.employee;

import java.time.Instant;

public class DownloadJobResponse {

    private String jobId;
    private String fileName;
    private DownloadJobStatus status;
    private String presignedDownloadUrl;
    private Instant urlExpiresAt;
    private String errorMessage;
    private Instant createdAt;

    public static DownloadJobResponse from(DownloadJob job) {
        DownloadJobResponse r = new DownloadJobResponse();
        r.jobId = job.getJobId();
        r.fileName = job.getFileName();
        r.status = job.getStatus();
        r.presignedDownloadUrl = job.getPresignedDownloadUrl();
        r.urlExpiresAt = job.getUrlExpiresAt();
        r.errorMessage = job.getErrorMessage();
        r.createdAt = job.getCreatedAt();
        return r;
    }

    public String getJobId() { return jobId; }
    public String getFileName() { return fileName; }
    public DownloadJobStatus getStatus() { return status; }
    public String getPresignedDownloadUrl() { return presignedDownloadUrl; }
    public Instant getUrlExpiresAt() { return urlExpiresAt; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
}
