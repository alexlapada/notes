package ua.alexlapada.integration;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import ua.alexlapada.EnvUtil;
import ua.alexlapada.constant.EnvKeys;
import ua.alexlapada.exception.AwsClientException;

@Slf4j
public class AwsS3Client {

    private static AwsS3Client instance;
    private final S3Client s3Client;
    private final String bucketName;

    private AwsS3Client() {
        this.s3Client = S3Client.builder()
                .region(Region.of(EnvUtil.getEnv(EnvKeys.AWS_REGION, EnvUtil.DEFAULT_REGION)))
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
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            ResponseBytes<GetObjectResponse> response = this.s3Client.getObjectAsBytes(getObjectRequest);
            byte[] content = response.asByteArray();
            log.info("Got content from [{}] with key [{}] successfully.", bucketName, key);
            return content;
        } catch (Exception e) {
            log.error("Get content from [{}] with key [{}] error.", bucketName, key, e);
            throw new AwsClientException(String.format("Get content from [%s] with key [%s] error. %s", bucketName, key, e.getMessage()));
        }
    }
}
