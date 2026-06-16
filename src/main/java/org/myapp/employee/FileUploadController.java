package org.myapp.employee;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final AsyncUploadService asyncUploadService;
    private final AsyncDownloadService asyncDownloadService;

    public FileUploadController(AsyncUploadService asyncUploadService,
                                AsyncDownloadService asyncDownloadService) {
        this.asyncUploadService = asyncUploadService;
        this.asyncDownloadService = asyncDownloadService;
    }

    // ─── Upload ──────────────────────────────────────────────────────────────

    /**
     * Accepts a file and returns 202 immediately with a jobId.
     * Background thread uploads to S3 and generates a presigned download URL.
     * Poll /api/files/upload/status/{jobId} to get the result.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadJobResponse> upload(@RequestParam("file") MultipartFile file) {
        try {
            String jobId = asyncUploadService.initiateUpload(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            );
            return ResponseEntity.accepted().body(UploadJobResponse.accepted(jobId, file.getOriginalFilename()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Poll the status of an async upload job.
     * When COMPLETED the response contains a presigned GET URL to download the file.
     */
    @GetMapping("/upload/status/{jobId}")
    public ResponseEntity<?> uploadStatus(@PathVariable("jobId") String jobId) {
        UploadJob job = asyncUploadService.getJob(jobId);
        if (job == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Upload job not found: " + jobId));
        }
        return ResponseEntity.ok(UploadJobResponse.from(job));
    }

    /**
     * Upload multiple files asynchronously. Returns a jobId per file.
     */
    @PostMapping(value = "/upload-batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<UploadJobResponse>> uploadBatch(@RequestParam("files") MultipartFile[] files) {
        try {
            List<UploadJobResponse> responses = new ArrayList<>();
            for (MultipartFile file : files) {
                String jobId = asyncUploadService.initiateUpload(
                        file.getOriginalFilename(),
                        file.getContentType(),
                        file.getBytes()
                );
                responses.add(UploadJobResponse.accepted(jobId, file.getOriginalFilename()));
            }
            return ResponseEntity.accepted().body(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ─── Download ─────────────────────────────────────────────────────────────

    /**
     * Initiate an async download for a file already in S3.
     * Returns 202 with a jobId. Poll /api/files/download/status/{jobId} for the presigned URL.
     */
    @PostMapping("/download/initiate")
    public ResponseEntity<Map<String, String>> initiateDownload(@RequestParam("fileName") String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "fileName is required"));
        }
        String jobId = asyncDownloadService.initiateDownload(fileName);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of(
                        "jobId", jobId,
                        "fileName", fileName,
                        "statusUrl", "/api/files/download/status/" + jobId
                ));
    }

    /**
     * Poll the status of an async download job.
     */
    @GetMapping("/download/status/{jobId}")
    public ResponseEntity<?> downloadStatus(@PathVariable("jobId") String jobId) {
        DownloadJob job = asyncDownloadService.getJob(jobId);
        if (job == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Download job not found: " + jobId));
        }
        return ResponseEntity.ok(DownloadJobResponse.from(job));
    }
}
