package cn.hiboot.mcn.autoconfigure.jpa;

import cn.hiboot.mcn.core.util.McnUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.lang.NonNull;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ExampleSpecification
 *
 * @author DingHao
 * @since 2022/1/22 11:25
 */
public class ExampleSpecification<T> implements Specification<T> {

    private final List<PredicateProvider<T>> predicateProviders;
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
        this.predicateProviders = Arrays.stream(predicateProviders).filter(Objects::nonNull).collect(Collectors.toList());
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
        if(example == null && McnUtils.isNullOrEmpty(predicateProviders)){
            return null;
        }
        Predicate beanPredicate = null;
        if(example != null){
            beanPredicate = criteriaBuilder.and(QueryByExamplePredicateBuilder.getPredicate(root, criteriaBuilder, example, escapeCharacter));
        }
        Predicate[] predicates =  predicates(predicateProviders,root,criteriaBuilder);
        Predicate logicPredicate = null;
        if(McnUtils.isNotNullAndEmpty(predicates)){
            logicPredicate = isOr ? criteriaBuilder.or(predicates) : criteriaBuilder.and(predicates);
        }
        if(beanPredicate == null && logicPredicate == null){
            return null;
        }
        if(beanPredicate == null){
            return logicPredicate;
        }
        if(logicPredicate == null){
            return beanPredicate;
        }
        return isOr ? criteriaBuilder.or(beanPredicate,logicPredicate) : criteriaBuilder.and(beanPredicate,logicPredicate);
    }

    private Predicate[] predicates(List<PredicateProvider<T>> predicateProviders,Root<T> root, CriteriaBuilder criteriaBuilder){
        if(McnUtils.isNullOrEmpty(predicateProviders)){
            return null;
        }
        List<Predicate> predicates =  new ArrayList<>(predicateProviders.size());
        for (PredicateProvider<T> predicateProvider : predicateProviders) {
            Predicate predicate = predicateProvider.getPredicate(root, criteriaBuilder);
            if(predicate == null){
                continue;
            }
            predicates.add(predicate);
        }
        return predicates.toArray(new Predicate[0]);
    }


}
