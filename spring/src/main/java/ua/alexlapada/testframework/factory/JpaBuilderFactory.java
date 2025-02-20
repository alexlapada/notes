package ua.alexlapada.testframework.factory;


import ua.alexlapada.testframework.factory.core.BuilderFactory;
import ua.alexlapada.testframework.factory.core.BuilderGuesser;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public abstract class JpaBuilderFactory<T, B> extends BaseFactory<T> implements BuilderFactory<T, B>, BuilderGuesser {

    @Override
    public B builder() {
        return getBuilder();
    }

    @Override
    public Map<String, Object> definition() {
        return Map.of();
    }

    @Override
    protected T getEntityObject() {
        return getEntityObject(JpaBuilderFactory.class);
    }

    @SuppressWarnings("unchecked")
    B getBuilder() {
        try {
            Class<?> rawClass = getEntityClass();

            assert rawClass != null;

            String builderMethod = guessBuilderMethod(rawClass);

            return (B) rawClass.getMethod(builderMethod).invoke(rawClass);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    protected Class<T> getEntityClass() {
        return getEntityClass(JpaBuilderFactory.class);
    }
}
