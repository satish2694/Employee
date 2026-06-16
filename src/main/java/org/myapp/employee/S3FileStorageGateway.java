package org.myapp.employee;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.DeleteObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedDeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@Service
public class S3FileStorageGateway {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final Duration presignedUrlExpiration;

    public S3FileStorageGateway(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.presignedUrlExpiration = Duration.ofMinutes(15);
    }

    public S3Client getS3Client() {
        return s3Client;
    }

    public S3Presigner getS3Presigner() {
        return s3Presigner;
    }

    public Duration getPresignedUrlExpiration() {
        return presignedUrlExpiration;
    }

    /**
     * Upload a file to S3 bucket
     *
     * @param bucket       S3 bucket name
     * @param key          File key/name in S3
     * @param contentType  MIME type of the file
     * @param data         File content as bytes
     */
    public void uploadToS3(String bucket, String key, String contentType, byte[] data) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));
    }

    /**
     * Generate a presigned delete request for a file in S3
     * This allows clients to delete the file using the presigned URL
     *
     * @param bucket S3 bucket name
     * @param key    File key/name in S3
     * @return PresignedDeleteObjectRequest containing the presigned URL
     */
    public PresignedDeleteObjectRequest generatePresignedDeleteRequest(String bucket, String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        DeleteObjectPresignRequest deletePresignRequest = DeleteObjectPresignRequest.builder()
                .deleteObjectRequest(deleteObjectRequest)
                .signatureDuration(presignedUrlExpiration)
                .build();

        return s3Presigner.presignDeleteObject(deletePresignRequest);
    }

    /**
     * Verify that an object exists in S3 (throws NoSuchKeyException if not found).
     */
    public void assertObjectExists(String bucket, String key) {
        s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
    }

    /**
     * Generate a presigned GET URL allowing a client to download the object directly from S3.
     */
    public PresignedGetObjectRequest generatePresignedGetRequest(String bucket, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest getPresignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(presignedUrlExpiration)
                .build();

        return s3Presigner.presignGetObject(getPresignRequest);
    }

    /**
     * Upload file to S3 and return a presigned delete request
     *
     * @param bucket       S3 bucket name
     * @param key          File key/name in S3
     * @param contentType  MIME type of the file
     * @param data         File content as bytes
     * @return PresignedDeleteObjectRequest for the uploaded file
     */
    public PresignedDeleteObjectRequest uploadAndGetPresignedDelete(String bucket, String key, String contentType, byte[] data) {
        uploadToS3(bucket, key, contentType, data);
        return generatePresignedDeleteRequest(bucket, key);
    }
}

