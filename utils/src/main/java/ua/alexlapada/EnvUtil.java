package ua.alexlapada;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EnvUtil {
    public static final String DEFAULT_REGION = "eu-central-1";

    public static String getEnv(String key) {
        return Optional.ofNullable(System.getenv(key))
                .filter(value -> !StringUtils.isBlank(value))
                .orElseThrow(() -> new IllegalArgumentException(String
                        .format("Required variable %s is missing in environment.", key)));
    }

    public static Integer getEnvAsInt(String key) {
        return Optional.ofNullable(System.getenv(key))
                .map(Integer::valueOf)
                .orElseThrow(() -> new IllegalArgumentException(String
                        .format("Required variable %s is missing in environment.", key)));
    }

    public static String getEnv(String key, String defaultValue) {
        return Optional.ofNullable(System.getenv(key))
                .filter(value -> !StringUtils.isBlank(value))
                .orElse(defaultValue);
    }

}
