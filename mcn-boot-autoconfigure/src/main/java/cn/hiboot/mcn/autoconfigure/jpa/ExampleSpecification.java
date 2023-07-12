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
import java.util.Collections;
import java.util.List;

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

    ExampleSpecification(PredicateProvider<T> predicateProvider) {
        this(Collections.singletonList(predicateProvider));
    }

    ExampleSpecification(List<PredicateProvider<T>> predicateProviders) {
        this(null,predicateProviders);
    }

    ExampleSpecification(T t, PredicateProvider<T> predicateProvider) {
        this(t, Collections.singletonList(predicateProvider));
    }

    ExampleSpecification(T t, List<PredicateProvider<T>> predicateProviders) {
        this.predicateProviders = predicateProviders;
        if(!McnUtils.isFieldAllNull(t)){
            this.example = Example.of(t);
        }
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
        List<Predicate> predicates =  new ArrayList<>(predicateProviders.size());
        for (PredicateProvider<T> predicateProvider : predicateProviders) {
            Predicate predicate = predicateProvider.getPredicate(root, criteriaBuilder);
            if(predicate == null){
                continue;
            }
            predicates.add(predicate);
        }
        if(predicates.isEmpty()){
            return beanPredicate;
        }
        Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
        Predicate logicPredicate = isOr ? criteriaBuilder.or(predicatesArray) : criteriaBuilder.and(predicatesArray);
        if(beanPredicate == null){
            return logicPredicate;
        }
        return criteriaBuilder.and(beanPredicate,logicPredicate);
    }

    public ExampleSpecification<T> escapeCharacter(EscapeCharacter escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
        return this;
    }

    ExampleSpecification<T> or() {
        this.isOr = true;
        return this;
    }

}
