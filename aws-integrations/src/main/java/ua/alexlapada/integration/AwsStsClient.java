package ua.alexlapada.integration;

import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;
import ua.alexlapada.EnvUtil;
import ua.alexlapada.constant.EnvKeys;
import ua.alexlapada.exception.AwsClientException;

import java.util.UUID;

public class AwsStsClient {
    private static AwsStsClient instance;
    private final StsClient stsClient;

    private AwsStsClient() {
        stsClient = StsClient.builder()
                .region(Region.of(EnvUtil.getEnv(EnvKeys.AWS_REGION, EnvUtil.DEFAULT_REGION)))
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();
    }

    public static synchronized AwsStsClient instance() {
        if (instance == null) {
            instance = new AwsStsClient();
        }
        return instance;
    }

    public AwsSessionCredentials assumeRole(String roleArn) {
        try {
            AssumeRoleRequest roleRequest = AssumeRoleRequest.builder()
                    .roleArn(roleArn)
                    .roleSessionName(UUID.randomUUID().toString())
                    .build();
            AssumeRoleResponse roleResponse = stsClient.assumeRole(roleRequest);
            Credentials credentials = roleResponse.credentials();
            return AwsSessionCredentials.create(
                    credentials.accessKeyId(),
                    credentials.secretAccessKey(),
                    credentials.sessionToken());
        } catch (Exception e) {
            throw new AwsClientException(String.format("Assume role [%s] error. %s", roleArn, e.getMessage()));
        }
    }
}
