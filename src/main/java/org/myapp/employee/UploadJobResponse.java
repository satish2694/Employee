package org.myapp.employee;

import java.time.Instant;

public class UploadJobResponse {

    private String jobId;
    private String fileName;
    private DownloadJobStatus status;
    private String presignedDownloadUrl;
    private Instant urlExpiresAt;
    private String errorMessage;
    private Instant createdAt;
    private String statusUrl;

    public static UploadJobResponse accepted(String jobId, String fileName) {
        UploadJobResponse r = new UploadJobResponse();
        r.jobId = jobId;
        r.fileName = fileName;
        r.status = DownloadJobStatus.PENDING;
        r.statusUrl = "/api/files/upload/status/" + jobId;
        return r;
    }

    public static UploadJobResponse from(UploadJob job) {
        UploadJobResponse r = new UploadJobResponse();
        r.jobId = job.getJobId();
        r.fileName = job.getFileName();
        r.status = job.getStatus();
        r.presignedDownloadUrl = job.getPresignedDownloadUrl();
        r.urlExpiresAt = job.getUrlExpiresAt();
        r.errorMessage = job.getErrorMessage();
        r.createdAt = job.getCreatedAt();
        r.statusUrl = "/api/files/upload/status/" + job.getJobId();
        return r;
    }

    public String getJobId() { return jobId; }
    public String getFileName() { return fileName; }
    public DownloadJobStatus getStatus() { return status; }
    public String getPresignedDownloadUrl() { return presignedDownloadUrl; }
    public Instant getUrlExpiresAt() { return urlExpiresAt; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public String getStatusUrl() { return statusUrl; }
}
