package org.myapp.employee;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final FileRepository fileRepository;

    public FileUploadController(FileUploadService fileUploadService, FileRepository fileRepository) {
        this.fileUploadService = fileUploadService;
        this.fileRepository = fileRepository;
    }

    @PostMapping("/upload-multipart")
    public ResponseEntity<FileEntity> uploadMultipart(@RequestParam("file") MultipartFile file) throws Exception {
        FileEntity fileEntity = new FileEntity(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes()
        );
        return ResponseEntity.ok(fileRepository.save(fileEntity));
    }

    @PostMapping("/upload-and-send/{fileId}")
    public ResponseEntity<String> uploadAndSend(@PathVariable Long fileId) {
        // Thread 1 — returns acknowledgement immediately
        fileUploadService.uploadToS3(fileId);  // Thread 2 — async S3 upload
        return ResponseEntity.accepted().body("File upload to S3 initiated for fileId: " + fileId);
    }

    @PostMapping("/upload/{fileId}")
    public ResponseEntity<String> upload(@PathVariable Long fileId) {
        // Thread 1 — returns acknowledgement immediately
        fileUploadService.uploadToS3(fileId);  // Thread 2 — async S3 upload
        return ResponseEntity.accepted().body("File upload initiated for fileId: " + fileId);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<FileEntity> getFile(@PathVariable Long fileId) {
        return fileRepository.findById(fileId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllFiles() {
        return ResponseEntity.ok(fileRepository.findAll());
    }
}
