package ua.alexlapada.model;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.utils.ImmutableMap;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public enum ServiceInstanceState {
    STOPPED,
    STARTING,
    STOPPING,
    STARTED,
    UNDEFINED;

    private static final Map<ServiceType, StatusProvider> statusProviders = strategy();

    private static Map<ServiceType, StatusProvider> strategy() {
        final ImmutableMap.Builder<ServiceType, StatusProvider> builder = ImmutableMap.builder();
        builder
                .put(ServiceType.EC2, Ec2StatusProvider.instance())
                .put(ServiceType.APP_STREAM, AppStreamStatusProvider.instance())
                .put(ServiceType.RDS, RdsStatusProvider.instance());
        return builder.build();
    }

    public static ServiceInstanceState resolve(ServiceType type, String awsStatus) {
        String status = awsStatus.toLowerCase();
        StatusProvider provider = Optional.ofNullable(statusProviders.get(type))
                .orElseThrow(() -> new IllegalArgumentException("No status resolver presented for service " + type));
        if (provider.stoppedStatuses().contains(status)) {
            return STOPPED;
        }
        if (provider.startingStatuses().contains(status)) {
            return STARTING;
        }
        if (provider.stoppingStatuses().contains(status)) {
            return STOPPING;
        }
        if (provider.startedStatuses().contains(status)) {
            return STARTED;
        }
        return UNDEFINED;
    }


    private interface StatusProvider {
        Set<String> stoppedStatuses();
        Set<String> startingStatuses();
        Set<String> stoppingStatuses();
        Set<String> startedStatuses();
    }

    private static class Ec2StatusProvider implements StatusProvider {
        public static Ec2StatusProvider instance() {
            return new Ec2StatusProvider();
        }

        @Override
        public Set<String> stoppedStatuses() {
            return Set.of("stopped");
        }

        @Override
        public Set<String> startingStatuses() {
            return Set.of("pending");
        }

        @Override
        public Set<String> stoppingStatuses() {
            return Set.of("terminated", "shutting-down", "stopping");
        }

        @Override
        public Set<String> startedStatuses() {
            return Set.of("running");
        }
    }

    private static class AppStreamStatusProvider implements StatusProvider {
        public static AppStreamStatusProvider instance() {
            return new AppStreamStatusProvider();
        }

        @Override
        public Set<String> stoppedStatuses() {
            return Set.of("stopped");
        }

        @Override
        public Set<String> startingStatuses() {
            return Set.of("starting");
        }

        @Override
        public Set<String> stoppingStatuses() {
            return Set.of("stopping");
        }

        @Override
        public Set<String> startedStatuses() {
            return Set.of("running");
        }
    }

    private static class RdsStatusProvider implements StatusProvider {
        public static RdsStatusProvider instance() {
            return new RdsStatusProvider();
        }

        @Override
        public Set<String> stoppedStatuses() {
            return Set.of("stopped");
        }

        @Override
        public Set<String> startingStatuses() {
            return Set.of("starting", "creating", "configuring-enhanced-monitoring",
                    "configuring-iam-database-auth", "configuring-log-exports", "modifying", "rebooting",
                    "resetting-master-credentials", "renaming", "storage-optimization", "upgrading", "backing-up");
        }

        @Override
        public Set<String> stoppingStatuses() {
            return Set.of("stopping");
        }

        @Override
        public Set<String> startedStatuses() {
            return Set.of("available");
        }
    }
}
