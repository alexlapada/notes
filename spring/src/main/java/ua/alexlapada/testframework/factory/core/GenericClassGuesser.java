package ua.alexlapada.testframework.factory.core;

import org.springframework.core.ResolvableType;

import java.lang.reflect.Constructor;

public interface GenericClassGuesser<E> {

    @SuppressWarnings("unchecked")
    default Class<E> getEntityClass(Class<?> clazz) {
        Class<?> rawClass =
                ResolvableType.forClass(getClass()).as(clazz).getGeneric(0).getRawClass();

        return (Class<E>) rawClass;
    }

    default E getEntityObject(Class<?> clazz) {
        try {
            Constructor<E> constructor = getEntityClass(clazz).getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
