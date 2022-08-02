package ua.alexlapada.integration;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import ua.alexlapada.EnvUtil;
import ua.alexlapada.constant.EnvKeys;
import ua.alexlapada.exception.AwsClientException;

import java.io.File;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class AwsS3Client {

    private static AwsS3Client instance;
    private final S3Client s3Client;
    private final String bucketName;
    private final Region awsRegion;

    private AwsS3Client() {
        this.awsRegion = Region.of(EnvUtil.getEnv(EnvKeys.AWS_REGION, EnvUtil.DEFAULT_REGION));
        this.s3Client = S3Client.builder()
                .region(awsRegion)
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();
        this.bucketName = EnvUtil.getEnv(EnvKeys.BUCKET_NAME);
    }

    public static AwsS3Client instance() {
        if (instance == null) {
            instance = new AwsS3Client();
        }
        return instance;
    }

    public byte[] getFileContent(String key) {
        return getFileContent(null, key);
    }

    public byte[] getFileContent(String bucket, String key) {
        String targetBucket = bucket == null ? bucketName : bucket;
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(targetBucket)
                    .key(key)
                    .build();
            ResponseBytes<GetObjectResponse> response = this.s3Client.getObjectAsBytes(getObjectRequest);
            byte[] content = response.asByteArray();
            log.info("Got content from [{}] with key [{}] successfully.", bucketName, key);
            return content;
        } catch (NoSuchKeyException e) {
            log.error("Get content from [{}] with key [{}] error.", bucketName, key, e);
            throw new AwsClientException(String.format("Content in [%s] with key [%s] not found. %s", bucketName, key, e.getMessage()));
        } catch (Exception e) {
            log.error("Get content from [{}] with key [{}] error.", bucketName, key, e);
            throw new AwsClientException(String.format("Get content from [%s] with key [%s] error. %s", bucketName, key, e.getMessage()));
        }
    }

    public boolean copyObject(String fromKey, String toKey) {
        try {
            CopyObjectResponse response = s3Client.copyObject(CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(fromKey)
                    .destinationBucket(bucketName)
                    .destinationKey(toKey)
                    .build());
            return response.sdkHttpResponse().isSuccessful();
        } catch (Exception e) {
            throw new AwsClientException(String.format("Moving file from [%s] to [%s] error. %s", fromKey, toKey, e.getMessage()));
        }
    }

    public boolean createFolder(String s3Key) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.empty());
            return response.sdkHttpResponse().isSuccessful();
        } catch (Exception e) {
            log.error("Folder creation error. {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean deleteObjects(Set<String> s3Keys) {
        try {
            Set<ObjectIdentifier> objectsToDelete = s3Keys.stream()
                    .map(key -> ObjectIdentifier.builder().key(key).build())
                    .collect(Collectors.toSet());
            DeleteObjectsResponse response = s3Client.deleteObjects(DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder()
                            .objects(objectsToDelete)
                            .quiet(true)
                            .build())
                    .build());
            return response.sdkHttpResponse().isSuccessful();
        } catch (Exception e) {
            log.error("Objects deletion error. {}", e.getMessage(), e);
            return false;
        }
    }

    public String createPresignedDownloadUrl(String s3Key) {
        try {
            S3Presigner presigner = S3Presigner.builder()
                    .region(awsRegion)
                    .build();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(30))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(getObjectPresignRequest);

            return presignedGetObjectRequest.url().toString();
        } catch (Exception e) {
            throw new AwsClientException(String.format("Error creating presigned download url for %s. %s", s3Key, e.getMessage()));
        }
    }

    public String createPresignedUploadUrl(String s3Key, String contentType) {
        S3Presigner presigner = S3Presigner.builder()
                .region(awsRegion)
                .build();
        try {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(30))
                    .putObjectRequest(objectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

            return presignedRequest.url().toString();
        } catch (Exception e) {
            throw new AwsClientException(String.format("Error creating presigned upload url for %s. %s", s3Key, e.getMessage()));
        }
    }

    public boolean deleteObject(String s3Key) {
        try {
            DeleteObjectsResponse response = s3Client.deleteObjects(DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder()
                            .objects(ObjectIdentifier.builder().key(s3Key).build())
                            .quiet(false)
                            .build())
                    .build());
            return response.sdkHttpResponse().isSuccessful();
        } catch (Exception e) {
            log.error("Delete file error. {}", e.getMessage(), e);
            return false;
        }
    }

    public ListObjectsV2Response describeObject(String s3Key) {
        return describeObject(s3Key, null);
    }

    public ListObjectsV2Response describeObject(String s3Key, String token) {
        return s3Client.listObjectsV2(getRequest(s3Key, token));
    }

    private ListObjectsV2Request getRequest(String s3Key, String token) {
        return ListObjectsV2Request.builder()
                .bucket(bucketName)
                .continuationToken(token)
                .prefix(s3Key)
                .delimiter(File.separator)
                .build();

    }
}
