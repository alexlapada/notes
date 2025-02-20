package ua.alexlapada.testframework.factory.core;

import lombok.Getter;
import org.modelmapper.ModelMapper;
import org.modelmapper.Provider;
import org.modelmapper.config.Configuration.AccessLevel;
import org.modelmapper.convention.MatchingStrategies;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class EntityAttrMapper<ENTITY> {

    private final ModelMapper modelMapper = new ModelMapper();

    @Getter
    private final Map<String, Object> attributes;

    private final ENTITY entity;

    public EntityAttrMapper(Map<String, Object> definition, Map<String, Object> additional, ENTITY entity) {
        init();

        this.attributes = merge(definition, additional);
        this.entity = entity;
    }

    protected void init() {
        Provider<Set> linkedHashSetProvider = request -> {
            Class<?> klass = request.getRequestedType();
            return klass.isAssignableFrom(Set.class) ? new HashSet<>() : null;
        };
        modelMapper
                .getConfiguration()
                .setProvider(linkedHashSetProvider)
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldAccessLevel(AccessLevel.PRIVATE)
                .setFieldMatchingEnabled(true)
                .setUseOSGiClassLoaderBridging(true);
    }

    public ENTITY getEntity() {
        return entity;
    }

    public EntityAttrMapper<ENTITY> fillByRawAttributes() {
        Map<String, Object> rawAttributes = getRawAttributes();
        ENTITY entity = getEntity();
        modelMapper.map(rawAttributes, entity);

        return this;
    }

    public void fillByBeforeSaveAttributes() {
        Map<String, BeforeSave<ENTITY>> beforeSaveAttributes = getBeforeSaveAttributes();

        beforeSaveAttributes.forEach((k, v) -> modelMapper.map(Map.of(k, v.action(this)), getEntity()));
    }

    public void fillByAfterSaveAttributes() {
        Map<String, AfterSave<ENTITY>> beforeSaveAttributes = getAfterSaveAttributes();

        beforeSaveAttributes.forEach((k, v) -> modelMapper.map(Map.of(k, v.action(this)), getEntity()));
    }

    public Map<String, Object> getRawAttributes() {
        Map<String, Object> result = new LinkedHashMap<>();

        attributes.forEach((key, value) -> {
            if (value instanceof BeforeSave<?>) {
                return;
            }

            if (value instanceof AfterSave<?>) {
                return;
            }

            result.put(key, value);
        });

        return result;
    }

    @SuppressWarnings("unchecked")
    public Map<String, BeforeSave<ENTITY>> getBeforeSaveAttributes() {
        Map<String, BeforeSave<ENTITY>> result = new LinkedHashMap<>();

        attributes.forEach((key, value) -> {
            if (value instanceof BeforeSave<?>) {
                result.put(key, (BeforeSave<ENTITY>) value);
            }
        });

        return result;
    }

    @SuppressWarnings("unchecked")
    public Map<String, AfterSave<ENTITY>> getAfterSaveAttributes() {
        Map<String, AfterSave<ENTITY>> result = new LinkedHashMap<>();

        attributes.forEach((key, value) -> {
            if (value instanceof AfterSave<?>) {
                result.put(key, (AfterSave<ENTITY>) value);
            }
        });

        return result;
    }

    protected Map<String, Object> merge(Map<String, Object> m1, Map<String, Object> m2) {
        Map<String, Object> result = new LinkedHashMap<>();

        result.putAll(m1);
        result.putAll(m2);

        return result;
    }
}
