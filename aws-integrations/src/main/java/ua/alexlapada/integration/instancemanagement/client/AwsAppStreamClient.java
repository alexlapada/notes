package ua.alexlapada.integration.instancemanagement.client;

import ua.alexlapada.EnvUtil;
import ua.alexlapada.constant.EnvKeys;
import ua.alexlapada.exception.AwsClientException;
import ua.alexlapada.integration.instancemanagement.AwsClientFactory;
import ua.alexlapada.integration.instancemanagement.AwsInstanceManagementClient;
import ua.alexlapada.model.ServiceInstance;
import ua.alexlapada.transformer.AppStreamInstanceTransformer;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.appstream.AppStreamClient;
import software.amazon.awssdk.services.appstream.model.DescribeFleetsRequest;
import software.amazon.awssdk.services.appstream.model.DescribeFleetsResponse;
import software.amazon.awssdk.services.appstream.model.StartFleetRequest;
import software.amazon.awssdk.services.appstream.model.StopFleetRequest;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AwsAppStreamClient implements AwsInstanceManagementClient {

    private final AppStreamClient appStreamClient;

    private AwsAppStreamClient(AwsSessionCredentials credentials) {
        this.appStreamClient = AppStreamClient.builder()
                .region(Region.of(EnvUtil.getEnv(EnvKeys.AWS_REGION, EnvUtil.DEFAULT_REGION)))
                .httpClient(UrlConnectionHttpClient.builder().build())
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    private static class Factory implements AwsClientFactory {
        public AwsAppStreamClient getClient(AwsSessionCredentials credentials) {
            return new AwsAppStreamClient(credentials);
        }
    }

    public static Factory factory() {
        return new Factory();
    }

    @Override
    public void startInstance(String instanceId) {
        try {
            StartFleetRequest request = StartFleetRequest.builder()
                    .name(instanceId)
                    .build();
            appStreamClient.startFleet(request);
            log.info("Successfully started instance {}", instanceId);
        } catch (Exception e) {
            log.error("Starting instance error. {}", e.getMessage());
            throw new AwsClientException("Starting instance error.", e);
        }
    }

    @Override
    public void stopInstance(String instanceId) {
        try {
            StopFleetRequest request = StopFleetRequest.builder()
                    .name(instanceId)
                    .build();
            appStreamClient.stopFleet(request);
            log.info("Successfully stopped instance {}", instanceId);
        } catch (Exception e) {
            log.error("Stopping instance error. {}", e.getMessage());
            throw new AwsClientException("Stopping instance error.", e);
        }
    }

    @Override
    public List<ServiceInstance> describeService(String instanceName) {
        DescribeFleetsResponse fleetsResponse = appStreamClient.describeFleets(DescribeFleetsRequest.builder()
                .names(instanceName)
                .build());
        return fleetsResponse.fleets()
                .stream()
                .map(AppStreamInstanceTransformer::transform)
                .collect(Collectors.toList());
    }
}
