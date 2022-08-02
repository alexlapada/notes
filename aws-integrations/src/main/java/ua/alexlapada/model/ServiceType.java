package ua.alexlapada.model;

import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public enum ServiceType {
    API_GATEWAY("api_gateway"),
    EC2("ec2"),
    RDS("rds"),
    LAMBDA_FUNCTION("lambda_function"),
    VPN("vpn"),
    APP_STREAM("app_stream");

    private final String name;

    private static final Map<String, ServiceType> VALUES = Stream.of(values())
            .collect(collectingAndThen(toMap(ServiceType::extension, Function.identity()), Collections::unmodifiableMap));

    public String extension() {
        return this.name;
    }

    public static ServiceType byName(final String name) {
        return Optional.ofNullable(VALUES.get(name.toLowerCase()))
                .orElseThrow(() -> new IllegalArgumentException(format("ServiceType for %s has not presented!", name)));
    }
}
