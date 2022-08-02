package ua.alexlapada.querydsl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.jpa.repository.support.QuerydslJpaRepository;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

public class EnhancedQueryDslJpaRepositoryImpl<T, ID extends Serializable> extends QuerydslJpaRepository<T, ID>
        implements EnhancedQueryDslJpaRepository<T, ID> {
    private final Querydsl querydsl;
    private final EntityManager entityManager;

    public EnhancedQueryDslJpaRepositoryImpl(final JpaEntityInformation<T, ID> entityInformation,
                                             final EntityManager entityManager) {
        super(entityInformation, entityManager);
        final EntityPath<T> path = SimpleEntityPathResolver.INSTANCE.createPath(entityInformation.getJavaType());
        this.querydsl = new Querydsl(entityManager, new PathBuilder<>(path.getType(), path.getMetadata()));
        this.entityManager = entityManager;
    }

    @Override
    public <R> Page<R> findAll(final Expression<R> expression, final Pageable pageable, final Predicate... predicates) {
        Assert.notNull(pageable, "Pageable must not be null!");
        assertExpression(expression);

        final JPQLQuery<?> countQuery = createCountQuery(predicates);
        final JPQLQuery<R> query = querydsl.applyPagination(pageable, createQuery(predicates).select(expression));

        return getPage(query.fetch(), pageable, countQuery::fetchCount);
    }

    @Override
    public <R> List<R> findAll(final Expression<R> expression, final Predicate... predicates) {
        assertExpression(expression);
        return createQuery(predicates).select(expression).fetch();
    }

    @Override
    public <R> Page<R> findAll(final Function<JPQLQuery<?>, JPQLQuery<R>> function, final Pageable pageable) {
        final JPQLQuery<R> query = function.apply(createQuery());
        final JPQLQuery<?> countQuery = createCountQuery(query.getMetadata().getWhere());

        return getPage(querydsl.applyPagination(pageable, query).fetch(), pageable, countQuery::fetchCount);
    }

    @Override
    public <R> List<R> findAll(final Function<JPQLQuery<?>, JPQLQuery<R>> function) {
        return function.apply(createQuery()).fetch();
    }

    @Override
    public <R> Page<R> findAll(final Function<JPQLQuery<?>, JPQLQuery<R>> function,
                               final Function<JPQLQuery<?>, JPQLQuery<?>> countFunction, final Pageable pageable) {
        final JPQLQuery<R> query = function.apply(createQuery());
        final JPQLQuery<?> countQuery = countFunction.apply(createCountQuery());

        return getPage(querydsl.applyPagination(pageable, query).fetch(), pageable, countQuery::fetchCount);
    }

    @Override
    public <R> List<R> findDistinct(final Expression<R> expression, final Predicate... predicates) {
        assertExpression(expression);
        return createQuery(predicates).select(expression).distinct().fetch();
    }

    @Override
    public <R> R find(final Expression<R> factoryExpression, final Predicate... predicates) {
        assertExpression(factoryExpression);
        return createQuery(predicates).select(factoryExpression).fetchOne();
    }

    @Override
    public <R> R find(final Function<JPQLQuery<?>, R> function) {
        return function.apply(createQuery());
    }

    @Override
    public void flushAndRefresh(T entity) {
        entityManager.flush();
        entityManager.refresh(entity);
    }

    private <R> void assertExpression(Expression<R> expression) {
        Assert.notNull(expression, "Expression must not be null!");
    }
}
