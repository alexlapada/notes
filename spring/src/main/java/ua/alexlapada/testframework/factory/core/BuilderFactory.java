package ua.alexlapada.testframework.factory.core;

import ua.alexlapada.testframework.factory.AbstractFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public interface BuilderFactory<T, B> {

    B builder();

    default void fillByBuilder(B b) {}

    default Map<String, ? extends Object> extractBuilderAttributes() {
        return extractAttributesFromBuilderState(this::fillByBuilder);
    }

    default <V extends AbstractFactory<T> & BuilderFactory<T, B>> V state(Consumer<B> function) {
        getStates().putAll(extractAttributesFromBuilderState(function));

        return (V) this;
    }

    default <V extends AbstractFactory<T> & BuilderFactory<T, B>> V sequence(Collection<Consumer<B>> functions) {

        getSequences()
                .addAll(functions.stream()
                        .map(this::extractAttributesFromBuilderState)
                        .collect(Collectors.toList()));

        return (V) this;
    }

    default <V extends AbstractFactory<T> & BuilderFactory<T, B>> V sequence(Consumer<B> function) {
        getSequences().add(extractAttributesFromBuilderState(function));

        return (V) this;
    }

    private Map<String, ? extends Object> extractAttributesFromBuilderState(Consumer<B> function) {
        BuilderAttributeSpy builderAttributeSpy = new SimpleAttributeSpy(builder());

        @SuppressWarnings("unchecked")
        B proxy = (B) builderAttributeSpy.getProxy();
        function.accept(proxy);

        return builderAttributeSpy.getAttributes();
    }

    Map<String, Object> getStates();

    List<Map<String, ? extends Object>> getSequences();
}
