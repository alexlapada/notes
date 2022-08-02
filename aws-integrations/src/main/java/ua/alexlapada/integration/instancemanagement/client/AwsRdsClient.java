package ua.alexlapada.integration.instancemanagement.client;

import ua.alexlapada.EnvUtil;
import ua.alexlapada.constant.EnvKeys;
import ua.alexlapada.exception.AwsClientException;
import ua.alexlapada.integration.instancemanagement.AwsClientFactory;
import ua.alexlapada.integration.instancemanagement.AwsInstanceManagementClient;
import ua.alexlapada.model.ServiceInstance;
import ua.alexlapada.transformer.RdsInstanceTransformer;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesRequest;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;
import software.amazon.awssdk.services.rds.model.Filter;
import software.amazon.awssdk.services.rds.model.StartDbInstanceRequest;
import software.amazon.awssdk.services.rds.model.StopDbInstanceRequest;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AwsRdsClient implements AwsInstanceManagementClient {

    private final RdsClient rdsClient;

    private AwsRdsClient(AwsSessionCredentials credentials) {
        this.rdsClient = RdsClient.builder()
                .region(Region.of(EnvUtil.getEnv(EnvKeys.AWS_REGION, EnvUtil.DEFAULT_REGION)))
                .httpClient(UrlConnectionHttpClient.builder().build())
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    private static class Factory implements AwsClientFactory {
        public AwsRdsClient getClient(AwsSessionCredentials credentials) {
            return new AwsRdsClient(credentials);
        }
    }

    public static Factory factory() {
        return new Factory();
    }

    @Override
    public void startInstance(String instanceId) {
        try {
            StartDbInstanceRequest request = StartDbInstanceRequest.builder()
                    .dbInstanceIdentifier(instanceId)
                    .build();
            rdsClient.startDBInstance(request);
            log.info("Successfully started instance {}", instanceId);
        } catch (Exception e) {
            log.error("Starting instance error. {}", e.getMessage());
            throw new AwsClientException("Starting instance error.", e);
        }
    }

    @Override
    public void stopInstance(String instanceId) {
        try {
            StopDbInstanceRequest request = StopDbInstanceRequest.builder()
                    .dbInstanceIdentifier(instanceId)
                    .build();
            rdsClient.stopDBInstance(request);
            log.info("Successfully stopped instance {}", instanceId);
        } catch (Exception e) {
            log.error("Stopping instance error. {}", e.getMessage());
            throw new AwsClientException("Stopping instance error.", e);
        }
    }

    @Override
    public List<ServiceInstance> describeService(String instanceName) {
        DescribeDbInstancesResponse response = rdsClient.describeDBInstances(DescribeDbInstancesRequest.builder()
                .filters(Filter.builder()
                        .name("db-instance-id")
                        .values(instanceName).build())
                .build());
        return response.dbInstances()
                .stream()
                .map(RdsInstanceTransformer::transform)
                .collect(Collectors.toList());
    }
}
