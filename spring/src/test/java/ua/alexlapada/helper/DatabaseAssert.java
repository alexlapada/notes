package ua.alexlapada.helper;

import com.sun.istack.NotNull;
import lombok.NonNull;
import org.apache.commons.beanutils.PropertyUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Query;

public interface DatabaseAssert {

    ApplicationContext getApplicationContext();

    default Map<String, Object> attrs() {
        return new HashMap<>();
    }

    default void assertDatabaseHas(Object o) {
        assertDatabaseHas(o, "Record not found!");
    }

    default void assertDatabaseHas(Object o, String message) {
        if (existsInDatabase(o)) {
            return;
        }

        throw new RuntimeException(message);
    }

    default <T> void assertDatabaseHas(Class<T> entityClass, Map<String, Object> attrs) {
        assertDatabaseHas(entityClass, attrs, "Record not found!");
    }

    default <T> void assertDatabaseHas(Class<T> entityClass, Map<String, Object> attrs, String message) {
        if (existsInDatabase(mapAssertedEntity(entityClass, attrs), attrs)) {
            return;
        }

        throw new RuntimeException(
                String.format("%s Entity: '%s(%s)'", message, entityClass.getSimpleName(), attrs.toString()));
    }

    default void assertDatabaseMissing(Object o) {
        assertDatabaseMissing(
                o, String.format("Record '%s' found!", o.getClass().getName()));
    }

    default void assertDatabaseMissing(Object o, String message) {
        if (!existsInDatabase(o)) {
            return;
        }

        throw new RuntimeException(message);
    }

    default <T> void assertDatabaseMissing(Class<T> entityClass, Map<String, Object> attrs) {
        assertDatabaseMissing(entityClass, attrs, "Record found!");
    }

    default <T> void assertDatabaseMissing(Class<T> entityClass, Map<String, Object> attrs, String message) {

        if (existsInDatabase(mapAssertedEntity(entityClass, attrs), attrs)) {
            throw new RuntimeException(
                    String.format("%s Entity: '%s(%s)'", message, entityClass.getSimpleName(), attrs.toString()));
        }
    }

    default void assertDatabaseCount(Object o, int count) {
        assertDatabaseCount(o, Long.valueOf(count));
    }

    default void assertDatabaseCount(Object o, Long count) {
        assertEquals(count, getRepositoryForDatabaseAssert(o).count());
    }

    default <T> void assertDatabaseCount(Class<T> entityClass, int count) {
        assertEquals(
                Long.valueOf(count), getRepositoryForDatabaseAssert(entityClass).count());
    }

    default <T> void assertDatabaseCount(Class<T> entityClass, Map<String, Object> attrs, int count) {
        assertDatabaseCount(entityClass, attrs, Long.valueOf(count));
    }

    default <T> void assertDatabaseCount(Class<T> entityClass, Map<String, Object> attrs, Long count) {
        assertEquals(count, getCountResult(entityClass, attrs));
    }

    private boolean existsInDatabase(Object o) {
        return getRepositoryForDatabaseAssert(o).exists(Example.of(o));
    }

    private boolean existsInDatabase(Object o, Map<String, Object> attrs) {
        return getRepositoryForDatabaseAssert(o).exists(getExampleQuery(o, attrs));
    }

    private Long countInDatabase(Object o, Map<String, Object> attrs) {
        return getRepositoryForDatabaseAssert(o).count(getExampleQuery(o, attrs));
    }

    @NonNull
    private JpaRepository getRepositoryForDatabaseAssert(Object o) {
        return getRepositoryForDatabaseAssert(o.getClass());
    }

    @NonNull
    private <T> JpaRepository getRepositoryForDatabaseAssert(Class<T> tClass) {
        return (JpaRepository) new Repositories(getApplicationContext())
                .getRepositoryFor(tClass)
                .orElseThrow(() -> new NoSuchElementException(
                        String.format("Repository for entity '%s' not found!", tClass.getName())));
    }

    private <T> Object getCountResult(Class<T> entityClass, Map<String, Object> attrs) {
        String[] where = new String[attrs.size()];
        Object[] formatted = new Object[attrs.size() + 1];
        formatted[0] = entityClass.getName();
        int i = 0;
        for (var entry : attrs.entrySet()) {
            where[i] = nonNull(entry.getValue()) ? "%s=:" + entry.getKey() : "%s is null";
            formatted[i + 1] = entry.getKey();
            i++;
        }
        String sql = ("select COUNT(*) from %s where " + String.join(" and ", where));
        Query query = getApplicationContext().getBean(EntityManager.class).createQuery(String.format(sql, formatted));
        for (var entry : attrs.entrySet()) {
            if (isNull(entry.getValue())) {
                continue;
            }
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query.getSingleResult();
    }

    private <T> T mapAssertedEntity(Class<T> entityClass, Map<String, Object> attrs) {
        ModelMapper mapper = new ModelMapper();

        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        T entity = mapper.map(attrs, entityClass);

        // Manually set "id" because the object has no setter
        if (attrs.containsKey("id")) {
            ReflectionTestUtils.setField(entity, "id", attrs.get("id"));
        }

        return entity;
    }

    @NotNull
    private Example<Object> getExampleQuery(Object o, Map<String, Object> attrs) {
        Set<String> ignored = getIgnorePaths(o, attrs);

        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIncludeNullValues();

        if (!ignored.isEmpty()) {
            exampleMatcher = exampleMatcher.withIgnorePaths(ignored.toArray(new String[0]));
        }

        return Example.of(o, exampleMatcher);
    }

    // Returns all entity fields
    @SuppressWarnings("unchecked")
    private Set<String> getIgnorePaths(Object entityClass, Map<String, Object> attrs) {
        Set<String> result = getEntityFields(entityClass).stream()
                .map(Field::getName)
                .filter(name -> !attrs.containsKey(name))
                .collect(Collectors.toSet());

        try {
            // Ignore relations when attrs has nested attributes map
            for (Map.Entry<String, Object> entry : attrs.entrySet()) {
                final String key = entry.getKey();
                final Object value = entry.getValue();

                if (value instanceof Map<?, ?>) {
                    Class fieldClass =
                            (Class) entityClass.getClass().getDeclaredField(key).getGenericType();
                    if (!fieldClass.isAnnotationPresent(Entity.class)) {
                        continue;
                    }

                    Set<String> fields =
                            getIgnorePaths(fieldClass.getConstructor().newInstance(), (Map<String, Object>) value);

                    result.addAll(
                            fields.stream().map(f -> String.join(".", key, f)).collect(Collectors.toList()));
                } else {
                    try {
                        if (!value.getClass().isAnnotationPresent(Entity.class)) {
                            continue;
                        }
                    } catch (Exception e) {
                        continue;
                    }

                    for (Field field : getEntityFields(value)) {
                        try {
                            Object v = PropertyUtils.getProperty(value, field.getName());

                            if (v == null) {
                                result.add(String.join(".", key, field.getName()));
                            }
                        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            result.add(String.join(".", key, field.getName()));
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @NotNull
    private static <T> List<Field> getEntityFields(T entityClass) {
        List<Field> fields = new ArrayList<>();
        Class clazz = entityClass.getClass();

        while (clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }

        return fields;
    }
}
