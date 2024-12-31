package cn.hiboot.mcn.autoconfigure.jpa;

import cn.hiboot.mcn.core.util.McnUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * ExampleSpecification
 *
 * @author DingHao
 * @since 2022/1/22 11:25
 */
public class ExampleSpecification<T> implements Specification<T> {

    private final PredicateProvider<T>[] predicateProviders;
    private Example<T> example;
    private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;
    private boolean isOr;

    ExampleSpecification(T bean) {
        this(bean, (PredicateProvider<T>) null);
    }

    @SafeVarargs
    ExampleSpecification(PredicateProvider<T>... predicateProviders) {
        this(null, predicateProviders);
    }

    @SafeVarargs
    ExampleSpecification(T bean, PredicateProvider<T>... predicateProviders) {
        this.predicateProviders = predicateProviders;
        if (!McnUtils.isFieldAllNull(bean)) {
            this.example = Example.of(bean);
        }
    }

    ExampleSpecification<T> or() {
        this.isOr = true;
        return this;
    }

    public ExampleSpecification<T> escapeCharacter(EscapeCharacter escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
        return this;
    }

    @Override
    public Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder criteriaBuilder) {
        if (this.example == null && McnUtils.isNullOrEmpty(this.predicateProviders)) {
            return null;
        }
        Predicate beanPredicate = null;
        if (this.example != null) {
            beanPredicate = criteriaBuilder.and(QueryByExamplePredicateBuilder.getPredicate(root, criteriaBuilder, this.example, this.escapeCharacter));
        }
        Predicate[] predicates = predicates(this.predicateProviders, root, criteriaBuilder);
        Predicate logicPredicate = null;
        if (McnUtils.isNotNullAndEmpty(predicates)) {
            logicPredicate = isOr ? criteriaBuilder.or(predicates) : criteriaBuilder.and(predicates);
        }
        if (beanPredicate == null && logicPredicate == null) {
            return null;
        }
        if (beanPredicate == null) {
            return logicPredicate;
        }
        if (logicPredicate == null) {
            return beanPredicate;
        }
        return isOr ? criteriaBuilder.or(beanPredicate, logicPredicate) : criteriaBuilder.and(beanPredicate, logicPredicate);
    }

    private Predicate[] predicates(PredicateProvider<T>[] predicateProviders, Root<T> root, CriteriaBuilder criteriaBuilder) {
        if (McnUtils.isNullOrEmpty(predicateProviders)) {
            return null;
        }
        List<Predicate> predicates = new ArrayList<>(predicateProviders.length);
        for (PredicateProvider<T> predicateProvider : predicateProviders) {
            Predicate predicate = predicateProvider.getPredicate(root, criteriaBuilder);
            if (predicate == null) {
                continue;
            }
            predicates.add(predicate);
        }
        return predicates.toArray(new Predicate[0]);
    }

}
