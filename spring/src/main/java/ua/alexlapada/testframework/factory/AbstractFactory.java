package ua.alexlapada.testframework.factory;

import lombok.Getter;
import ua.alexlapada.testframework.factory.core.BuilderFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

public abstract class AbstractFactory<T> {

    protected static final Random random = new Random();

    @Getter
    protected Map<String, Object> states = new HashMap<>();

    @Getter
    protected List<Map<String, ? extends Object>> sequences = new ArrayList<>();

    protected Iterator<? extends Map<String, ? extends Object>> sequenceIterator = null;

    public abstract Map<String, Object> definition();

    public abstract T create(Map<String, Object> attributes);

    public abstract List<T> createMany(Map<String, Object> attributes, Integer times);

    public T create() {
        return create(Map.of());
    }

    public List<T> createMany() {
        return createMany(2);
    }

    public List<T> createMany(Integer times) {
        return createMany(Map.of(), times);
    }

    public List<T> createSequences() {
        return createMany(Map.of(), sequences.size());
    }

    public T make() {
        return make(Map.of());
    }

    public T make(Map<String, Object> attributes) {
        throw new RuntimeException("\"make\" method is not allowed here, use \"create\" instead");
    }

    public List<T> makeMany() {
        return makeMany(1);
    }

    public List<T> makeMany(Integer times) {
        return makeMany(Map.of(), times);
    }

    public List<T> makeSequences() {
        return makeMany(Map.of(), sequences.size());
    }

    public List<T> makeMany(Map<String, Object> attributes, Integer times) {
        throw new RuntimeException("\"makeMany\" method is not allowed here, use \"createMany\" instead");
    }

    protected Object getStateValue(String key) {
        return states.get(key);
    }

    protected <V> V putStateIfEmpty(String key, V defaultValue) {
        return putStateIfEmpty(key, () -> defaultValue);
    }

    @SuppressWarnings("unchecked")
    protected <V> V putStateIfEmpty(String key, Supplier<V> defaultValue) {
        if (!states.containsKey(key)) {
            states.put(key, defaultValue.get());
        }

        return (V) states.get(key);
    }

    protected <E extends Enum<E>> E randomEnumValue(E[] values) {
        return values[random.nextInt(values.length)];
    }

    protected Map<String, Object> mergeDefinitionWithStates() {
        Map<String, Object> result = new LinkedHashMap<>();
        fillBuilderData(result);
        result.putAll(definition());
        result.putAll(getStates());

        return result;
    }

    protected void fillBuilderData(Map<String, Object> result) {
        if (this instanceof BuilderFactory<?, ?>) {
            result.putAll(((BuilderFactory<?, ?>) this).extractBuilderAttributes());
        }
    }

    protected <V extends AbstractFactory<T>> V state(Map<String, Object> states) {

        this.states.putAll(states);

        return (V) this;
    }

    public <V extends AbstractFactory<T>> V state(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);

        return state(map);
    }

    public <V extends AbstractFactory<T>> V sequence(List<Map<String, ? extends Object>> sequences) {
        this.sequences.addAll(sequences);

        return (V) this;
    }

    public <V extends AbstractFactory<T>> V sequence(Map<String, ?> sequence) {
        return sequence(List.of(sequence));
    }

    public <V extends AbstractFactory<T>> V sequence(String field, Object value) {
        return sequence(Map.of(field, value));
    }

    protected Map<String, Object> mergeAttributes(Map<String, Object> attributes) {
        Map<String, Object> result = mergeDefinitionWithStates();
        result.putAll(attributes);

        return result;
    }

    protected void clearState() {
        states = new HashMap<>();
        sequences = new ArrayList<>();
        sequenceIterator = null;
    }

    protected <I> I getClassInstance(Class<I> factoryClass) {
        try {
            return factoryClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
