package org.myapp.employee;

import java.time.Instant;
import java.util.Map;

/**
 * Response DTO containing presigned delete URL details
 */
public class PresignedDeleteResponse {

    private Long fileId;
    private String fileName;
    private String presignedUrl;
    private Instant expiresAt;
    private Map<String, String> httpHeaders;

    public PresignedDeleteResponse(Long fileId, String fileName, String presignedUrl, Instant expiresAt, Map<String, String> httpHeaders) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.presignedUrl = presignedUrl;
        this.expiresAt = expiresAt;
        this.httpHeaders = httpHeaders;
    }

    // Getters
    public Long getFileId() {
        return fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPresignedUrl() {
        return presignedUrl;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Map<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    // Setters
    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setPresignedUrl(String presignedUrl) {
        this.presignedUrl = presignedUrl;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setHttpHeaders(Map<String, String> httpHeaders) {
        this.httpHeaders = httpHeaders;
    }
}

