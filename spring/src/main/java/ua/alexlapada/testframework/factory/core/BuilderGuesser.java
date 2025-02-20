package ua.alexlapada.testframework.factory.core;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public interface BuilderGuesser {
    String[] BUILDER_METHODS = {"builder", "newBuilder"};

    default String[] getBuilderMethods() {
        return BUILDER_METHODS;
    }

    default String guessBuilderMethod(Class<?> rawClass) {
        Set<String> methods =
                Arrays.stream(rawClass.getMethods()).map(Method::getName).collect(Collectors.toSet());

        final String[] builderMethods = getBuilderMethods();

        for (String builderMethod : builderMethods) {
            if (methods.contains(builderMethod)) {
                return builderMethod;
            }
        }

        throw new RuntimeException(String.format(
                "Class '%s' doesn't have any supported builder methods of: %s",
                rawClass.getSimpleName(), String.join(", ", builderMethods)));
    }
}
