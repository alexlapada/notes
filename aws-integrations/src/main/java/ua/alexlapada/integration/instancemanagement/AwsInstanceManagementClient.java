package ua.alexlapada.integration.instancemanagement;

import ua.alexlapada.model.ServiceInstance;

import java.util.List;

public interface AwsInstanceManagementClient {
    void startInstance(String instanceId);

    void stopInstance(String instanceId);

    List<ServiceInstance> describeService(String instanceName);
}
