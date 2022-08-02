package ua.alexlapada.integration.instancemanagement;

import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;

public interface AwsClientFactory {
    AwsInstanceManagementClient getClient(AwsSessionCredentials credentials);
}
