package ua.alexlapada.integration.instancemanagement.client;

import ua.alexlapada.EnvUtil;
import ua.alexlapada.constant.EnvKeys;
import ua.alexlapada.integration.instancemanagement.AwsClientFactory;
import ua.alexlapada.integration.instancemanagement.AwsInstanceManagementClient;
import ua.alexlapada.model.ServiceInstance;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;

import java.util.List;

public class AwsAutoscalingClient implements AwsInstanceManagementClient {
    private final AutoScalingClient autoScalingClient;

    private AwsAutoscalingClient(AwsSessionCredentials credentials) {
        this.autoScalingClient = AutoScalingClient.builder()
                .region(Region.of(EnvUtil.getEnv(EnvKeys.AWS_REGION, EnvUtil.DEFAULT_REGION)))
                .httpClient(UrlConnectionHttpClient.builder().build())
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    private static class Factory implements AwsClientFactory {
        public AwsAutoscalingClient getClient(AwsSessionCredentials credentials) {
            return new AwsAutoscalingClient(credentials);
        }
    }

    @Override
    public void startInstance(String instanceId) {

    }

    @Override
    public void stopInstance(String instanceId) {

    }

    @Override
    public List<ServiceInstance> describeService(String instanceName) {
        return null;
    }
}
