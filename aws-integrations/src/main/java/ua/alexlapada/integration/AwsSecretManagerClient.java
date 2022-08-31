package ua.alexlapada.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import ua.alexlapada.EnvUtil;
import ua.alexlapada.JacksonUtil;
import ua.alexlapada.constant.EnvKeys;
import ua.alexlapada.exception.AwsClientException;

import java.util.Base64;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class AwsSecretManagerClient {
    private final SecretsManagerClient client;

    private AwsSecretManagerClient() {
        long start = System.currentTimeMillis();
        log.info("Secret manager configuration start.");
        this.client = SecretsManagerClient.builder()
                .region(Region.of(EnvUtil.getEnv(EnvKeys.AWS_REGION, EnvUtil.DEFAULT_REGION)))
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();
        log.info("Secret manager configuration finish. Duration: {} ms", System.currentTimeMillis() - start);
    }

    private AwsSecretManagerClient(AwsSessionCredentials credentials) {
        long start = System.currentTimeMillis();
        log.info("Secret manager configuration start.");
        this.client = SecretsManagerClient.builder()
                .region(Region.of(EnvUtil.getEnv(EnvKeys.AWS_REGION, EnvUtil.DEFAULT_REGION)))
                .httpClient(UrlConnectionHttpClient.builder().build())
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
        log.info("Secret manager configuration finish. Duration: {} ms", System.currentTimeMillis() - start);
    }

    public Map<String, Object> getSecret(String secretId) {
        try {
            GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                    .secretId(secretId)
                    .build();
            GetSecretValueResponse secretResponse = client.getSecretValue(getSecretValueRequest);
            String secret = !Objects.isNull(secretResponse.secretString()) ? secretResponse.secretString() :
                    new String(Base64.getDecoder().decode(secretResponse.secretBinary().asByteArray()));
            return JacksonUtil.read(secret, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Retrieve aws secret from SM error. " + e.getMessage());
            throw new AwsClientException("Retrieve aws secret from SM error", e);
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Factory {
        private static final AwsStsClient stsClient = AwsStsClient.instance();
        private static final String ASSUME_ROLE_ARN = EnvUtil.getEnv(EnvKeys.AWS_ASSUME_ROLE_ARN);
        private static AwsSecretManagerClient instance;

        public static AwsSecretManagerClient getClient() {
            AwsSessionCredentials credentials = stsClient.assumeRole(ASSUME_ROLE_ARN);
            return new AwsSecretManagerClient(credentials);
        }

        public static AwsSecretManagerClient getClientSingleton() {
            if (instance == null) {
                instance = new AwsSecretManagerClient();
            }
            return instance;
        }
    }

}
