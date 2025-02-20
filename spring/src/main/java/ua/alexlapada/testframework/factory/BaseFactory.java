package ua.alexlapada.testframework.factory;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ua.alexlapada.testframework.factory.core.AfterSave;
import ua.alexlapada.testframework.factory.core.BeforeSave;
import ua.alexlapada.testframework.factory.core.EntityAttrMapper;
import ua.alexlapada.testframework.factory.core.GenericClassGuesser;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Transactional
@Component
public abstract class BaseFactory<ENTITY> extends AbstractFactory<ENTITY> implements GenericClassGuesser<ENTITY> {

    @Getter
    @PersistenceContext
    protected EntityManager em;

    @Autowired
    protected ApplicationContext app;

    @Override
    public ENTITY create(Map<String, Object> attributes) {
        try {
            return doCreate(attributes);
        } finally {
            clearState();
        }
    }

    @Override
    public List<ENTITY> createMany(Integer times) {
        return super.createMany(times);
    }

    @Override
    public List<ENTITY> createSequences() {
        return createMany(Map.of(), sequences.size());
    }

    @Override
    public ENTITY create() {
        return super.create();
    }

    @Override
    public List<ENTITY> createMany() {
        return super.createMany();
    }

    @Override
    public List<ENTITY> createMany(Map<String, Object> attributes, Integer times) {
        List<ENTITY> result = new ArrayList<>();

        try {
            for (int i = 0; i < times; i++) {
                result.add(doCreate(attributes));
            }
        } finally {
            clearState();
        }

        return result;
    }

    protected ENTITY doCreate(Map<String, Object> attributes) {
        checkEntityManagerInstance();

        EntityAttrMapper<ENTITY> factoryCtx = doMake(attributes);

        ENTITY entity = factoryCtx.getEntity();

        factoryCtx.fillByBeforeSaveAttributes();

        beforeCreate(entity);

        saveAndFlush(entity);

        afterCreate(entity);

        return entity;
    }

    @Override
    public ENTITY make(Map<String, Object> attributes) {
        try {
            return doMake(attributes).getEntity();
        } finally {
            clearState();
        }
    }

    @Override
    public List<ENTITY> makeMany(Map<String, Object> attributes, Integer times) {
        List<ENTITY> result = new ArrayList<>();

        try {
            for (int i = 0; i < times; i++) {
                result.add(doMake(attributes).getEntity());
            }
        } finally {
            clearState();
        }

        return result;
    }

    protected EntityAttrMapper<ENTITY> doMake(Map<String, Object> attributes) {
        EntityAttrMapper<ENTITY> attrMapper =
                new EntityAttrMapper<>(mergeDefinitionWithStates(), attributes, getEntityObject());

        return attrMapper.fillByRawAttributes();
    }

    protected ENTITY getEntityObject() {
        return getEntityObject(BaseFactory.class);
    }

    protected void beforeCreate(ENTITY entity) {}

    protected void afterCreate(ENTITY entity) {}

    protected void save(ENTITY o) {
        em.persist(o);
    }

    protected void saveAndFlush(ENTITY entity) {
        save(entity);
        // TODO think about whether we need to flush or not
        //    flush(entity);
    }

    protected void flush(ENTITY entity) {
        em.flush();
        em.clear();
    }

    protected <T extends BaseFactory<?>> T getFactory(Class<T> factoryClass) {
        return app != null ? app.getBean(factoryClass) : getClassInstance(factoryClass);
    }

    /** Run deleteAllInBatch() for main ENTITY & usedEntities(). */
    public void clearStorage() {
        Repositories repositories = new Repositories(app);

        for (Class<?> entity : getEntitiesToClearStorage()) {
            Optional<Object> optionalRepository = repositories.getRepositoryFor(entity);

            if (optionalRepository.isEmpty()) {
                throw new RuntimeException(String.format("Can`t find repository for '%s' entity", entity.getName()));
            }

            JpaRepository<?, ?> repository = (JpaRepository<?, ?>) optionalRepository.get();

            repository.deleteAllInBatch();
        }
    }

    private Set<Class<?>> getEntitiesToClearStorage() {
        Set<Class<?>> entities = new LinkedHashSet<>(clearEntities());

        entities.add(getEntityObject().getClass());

        return entities;
    }

    protected List<Class<?>> clearEntities() {
        return List.of();
    }

    protected BeforeSave<ENTITY> beforeSave(BeforeSave<ENTITY> callable) {
        return callable;
    }

    protected AfterSave<ENTITY> afterSave(AfterSave<ENTITY> callable) {
        return callable;
    }

    private void checkEntityManagerInstance() {
        if (em != null) {
            return;
        }

        throw new RuntimeException(String.format(
                "Impossible to save entity '%s' without an EntityManager instance in the '%s' factory.",
                getEntityObject().getClass().getName(), this.getClass().getName()));
    }
}
