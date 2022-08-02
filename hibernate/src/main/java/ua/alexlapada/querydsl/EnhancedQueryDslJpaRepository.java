package ua.alexlapada.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

@NoRepositoryBean
public interface EnhancedQueryDslJpaRepository<T, ID extends Serializable>
        extends JpaRepository<T, ID>, QuerydslPredicateExecutor<T> {

    <R> Page<R> findAll(Expression<R> expression, Pageable pageable, Predicate... predicates);

    <R> List<R> findAll(Expression<R> expression, Predicate... predicates);

    <R> Page<R> findAll(Function<JPQLQuery<?>, JPQLQuery<R>> function, Pageable pageable);

    <R> List<R> findAll(Function<JPQLQuery<?>, JPQLQuery<R>> function);

    <R> Page<R> findAll(Function<JPQLQuery<?>, JPQLQuery<R>> function,
                        Function<JPQLQuery<?>, JPQLQuery<?>> countFunction, Pageable pageable);

    <R> List<R> findDistinct(Expression<R> factoryExpression, Predicate... predicates);

    <R> R find(Expression<R> factoryExpression, Predicate... predicates);

    <R> R find(Function<JPQLQuery<?>, R> function);

    void flushAndRefresh(T entity);
}
