package ua.alexlapada.transformer;

import ua.alexlapada.model.ServiceInstance;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Tag;
import ua.alexlapada.model.ServiceInstanceState;
import ua.alexlapada.model.ServiceType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Ec2InstanceTransformer {
    private static final String NAME_TAG_NAME = "Name";
    private static final ServiceType type = ServiceType.EC2;

    public static ServiceInstance transform(Instance instance) {
        ServiceInstance metaData = new ServiceInstance();
        metaData.setId(instance.instanceId());
        metaData.setState(ServiceInstanceState.resolve(type, instance.state().nameAsString()));
        metaData.setInstanceType(instance.instanceTypeAsString());
        metaData.setName(getTag(NAME_TAG_NAME, instance));
        metaData.setAwsState(instance.state().nameAsString());
        return metaData;
    }

    private static String getTag(String tagName, Instance instance) {
        return instance.tags().stream()
                .filter(tag -> tag.key().equals(tagName))
                .findAny()
                .map(Tag::value)
                .orElse(null);
    }
}
