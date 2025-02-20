package ua.alexlapada.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CollectionHelper {

    public static <T> List<T> filterByInstance(List<?> collection, Class<T> clazz) {
        return collection.stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
    }
}
