package ua.alexlapada.transformer;

import ua.alexlapada.model.ServiceInstance;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.appstream.model.Fleet;
import ua.alexlapada.model.ServiceInstanceState;
import ua.alexlapada.model.ServiceType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppStreamInstanceTransformer {
    private static final ServiceType type = ServiceType.APP_STREAM;

    public static ServiceInstance transform(Fleet fleet) {
        ServiceInstance metaData = new ServiceInstance();
        metaData.setId(fleet.name());
        metaData.setInstanceType(fleet.instanceType());
        metaData.setState(ServiceInstanceState.resolve(type, fleet.stateAsString()));
        metaData.setAwsState(fleet.stateAsString());
        metaData.setName(fleet.description());
        return metaData;
    }
}
