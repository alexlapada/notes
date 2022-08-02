package ua.alexlapada.integration.instancemanagement;

import ua.alexlapada.EnvUtil;
import ua.alexlapada.constant.EnvKeys;
import ua.alexlapada.integration.AwsStsClient;
import ua.alexlapada.integration.instancemanagement.client.AwsAppStreamClient;
import ua.alexlapada.integration.instancemanagement.client.AwsEc2Client;
import ua.alexlapada.integration.instancemanagement.client.AwsRdsClient;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.utils.ImmutableMap;
import ua.alexlapada.model.ServiceType;

import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AwsClientAbstractFactory {
    private static final AwsStsClient stsClient = AwsStsClient.instance();
    private static final String ASSUME_ROLE_NAME = EnvUtil.getEnv(EnvKeys.AWS_ASSUME_ROLE_ARN);

    private static final Map<ServiceType, AwsClientFactory> factories = factories();

    private static Map<ServiceType, AwsClientFactory> factories() {
        final ImmutableMap.Builder<ServiceType, AwsClientFactory> builder = ImmutableMap.builder();
        builder
                .put(ServiceType.EC2, AwsEc2Client.factory())
                .put(ServiceType.APP_STREAM, AwsAppStreamClient.factory())
                .put(ServiceType.RDS, AwsRdsClient.factory());
        return builder.build();
    }

    public static AwsInstanceManagementClient getClient(ServiceType type) {
        AwsClientFactory clientFactory = Optional.ofNullable(factories.get(type))
                .orElseThrow(() -> new IllegalArgumentException("Client Instance Factory not presented for Service " + type));
        AwsSessionCredentials credentials = stsClient.assumeRole(ASSUME_ROLE_NAME);
        return clientFactory.getClient(credentials);
    }
}
