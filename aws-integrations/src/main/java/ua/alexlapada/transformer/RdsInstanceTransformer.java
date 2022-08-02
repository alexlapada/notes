package ua.alexlapada.transformer;

import ua.alexlapada.model.ServiceInstance;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.rds.model.DBInstance;
import ua.alexlapada.model.ServiceInstanceState;
import ua.alexlapada.model.ServiceType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RdsInstanceTransformer {
    private static final ServiceType type = ServiceType.RDS;

    public static ServiceInstance transform(DBInstance instance) {
        ServiceInstance metaData = new ServiceInstance();
        metaData.setId(instance.dbInstanceIdentifier());
        metaData.setInstanceType(instance.dbInstanceClass());
        metaData.setState(ServiceInstanceState.resolve(type, instance.dbInstanceStatus()));
        metaData.setName(instance.dbName());
        metaData.setAwsState(instance.dbInstanceStatus());
        return metaData;
    }
}
