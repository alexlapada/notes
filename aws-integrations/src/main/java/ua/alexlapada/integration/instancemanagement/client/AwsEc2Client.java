package ua.alexlapada.integration.instancemanagement.client;

import ua.alexlapada.EnvUtil;
import ua.alexlapada.constant.EnvKeys;
import ua.alexlapada.exception.AwsClientException;
import ua.alexlapada.integration.instancemanagement.AwsClientFactory;
import ua.alexlapada.integration.instancemanagement.AwsInstanceManagementClient;
import ua.alexlapada.model.ServiceInstance;
import ua.alexlapada.transformer.Ec2InstanceTransformer;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.StartInstancesRequest;
import software.amazon.awssdk.services.ec2.model.StopInstancesRequest;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AwsEc2Client implements AwsInstanceManagementClient {

    private static AwsEc2Client instance;

    private final Ec2Client ec2Client;

    private AwsEc2Client(AwsSessionCredentials credentials) {
        this.ec2Client = Ec2Client.builder()
                .region(Region.of(EnvUtil.getEnv(EnvKeys.AWS_REGION, EnvUtil.DEFAULT_REGION)))
                .httpClient(UrlConnectionHttpClient.builder().build())
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    private static class Factory implements AwsClientFactory {
        public AwsEc2Client getClient(AwsSessionCredentials credentials) {
            return new AwsEc2Client(credentials);
        }
    }

    public static Factory factory() {
        return new Factory();
    }

    @Override
    public void startInstance(String instanceId) {
        try {
            StartInstancesRequest request = StartInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build();

            ec2Client.startInstances(request);
            log.info("Successfully started instance {}", instanceId);
        } catch (Exception e) {
            log.error("Starting instance error. {}", e.getMessage());
            throw new AwsClientException("Starting instance error.", e);
        }
    }

    @Override
    public void stopInstance(String instanceId) {
        try {
            StopInstancesRequest request = StopInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build();

            ec2Client.stopInstances(request);
            log.info("Successfully stopped instance {}", instanceId);
        } catch (Exception e) {
            log.error("Stopping instance error. {}", e.getMessage());
            throw new AwsClientException("Stopping instance error.", e);
        }
    }

    @Override
    public List<ServiceInstance> describeService(String instanceName) {
        DescribeInstancesResponse response = ec2Client.describeInstances(DescribeInstancesRequest.builder()
                .filters(Filter.builder()
                        .name("tag:Name")
                        .values(instanceName).build())
                .build());
        return response.reservations()
                .stream()
                .map(Reservation::instances)
                .flatMap(List::stream)
                .map(Ec2InstanceTransformer::transform)
                .collect(Collectors.toList());
    }
}
